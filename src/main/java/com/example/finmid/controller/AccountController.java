package com.example.finmid.controller;

import com.example.finmid.dto.AccountDto;
import com.example.finmid.dto.CreateAccountDto;
import com.example.finmid.response.AccountBalanceResponse;
import com.example.finmid.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public Mono<AccountDto> createAccount(@RequestBody CreateAccountDto account) {
        log.info("Received account creation request");
        return accountService.createAccount(account);
    }

    @GetMapping("{id}/balance")
    public Mono<AccountBalanceResponse> getBalance(@PathVariable Long id) {
        log.debug("Received get balance request for account {}", id);
        return accountService.getAccountBalance(id).map(balance -> AccountBalanceResponse.builder().balance(balance).build());
    }

    @PostMapping("transaction")
    public Mono<String> transferFunds(@RequestParam Long senderId, @RequestParam Long receiverId, @RequestParam BigDecimal amount) {
        log.info("Received transaction request for amount {}", amount);
        return accountService.executeTransaction(senderId, receiverId, amount).then(Mono.fromCallable(() -> "Transaction proceeded successfully"));
    }
}
