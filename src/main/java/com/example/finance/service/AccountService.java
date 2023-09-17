package com.example.finance.service;

import com.example.finance.converter.AccountConverter;
import com.example.finance.dto.AccountDto;
import com.example.finance.dto.CreateAccountDto;
import com.example.finance.entinty.AccountEntity;
import com.example.finance.entinty.TransactionEntity;
import com.example.finance.exception.AccountNotFoundException;
import com.example.finance.exception.InsufficientFundsException;
import com.example.finance.repository.AccountRepository;
import com.example.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.r2dbc.UncategorizedR2dbcException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.example.finance.converter.AccountConverter.createAccountToEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionalOperator transactionalOperator;

    public Mono<AccountDto> createAccount(CreateAccountDto account) {
        return accountRepository.save(createAccountToEntity(account))
                .doOnSuccess(entity -> log.info("Account created with id: {}", entity.getId()))
                .map(AccountConverter::entityToDto);
    }

    public Mono<BigDecimal> getAccountBalance(Long id) {
        return accountRepository.findById(id).map(AccountEntity::getBalance).switchIfEmpty(Mono.error(new AccountNotFoundException(id)));
    }

    public Mono<Void> executeTransaction(Long senderId, Long receiverId, BigDecimal amount) {
        if (senderId.equals(receiverId)) {
            return Mono.error(new IllegalArgumentException("Same sender and receiver, account id: " + senderId));
        }
        return transactionalOperator.execute(status -> accountRepository
                        .findById(senderId)
                        .switchIfEmpty(Mono.error(new AccountNotFoundException(senderId)))
                        .zipWith(accountRepository
                                .findById(receiverId)
                                .switchIfEmpty(Mono.error(new AccountNotFoundException(receiverId)))
                        )
                        .flatMap(tuple2 -> {
                            AccountEntity sender = tuple2.getT1();
                            AccountEntity receiver = tuple2.getT2();

                            if (sender.getBalance().compareTo(amount) < 0) {
                                return Mono.error(new InsufficientFundsException(senderId));
                            }

                            sender.setBalance(sender.getBalance().subtract(amount));
                            receiver.setBalance(receiver.getBalance().add(amount));

                            LocalDateTime modifyDate = LocalDateTime.now(ZoneOffset.UTC);

                            sender.setModifyDate(modifyDate);
                            receiver.setModifyDate(modifyDate);

                            Mono<AccountEntity> senderUpdate = accountRepository.save(sender);
                            Mono<AccountEntity> receiverUpdate = accountRepository.save(receiver);

                            return Mono.when(senderUpdate, receiverUpdate)
                                    .then(transactionRepository.save(
                                            TransactionEntity.builder()
                                                    .amount(amount)
                                                    .sender(senderId)
                                                    .receiver(receiverId)
                                                    .date(LocalDateTime.now(ZoneOffset.UTC))
                                                    .build()
                                    ))
                                    .doOnSuccess(entity -> log.info("Transaction proceeded successfully"));
                        })
                )
                .then()
                .retryWhen(Retry.backoff(50, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof OptimisticLockingFailureException || throwable instanceof UncategorizedR2dbcException));
    }
}
