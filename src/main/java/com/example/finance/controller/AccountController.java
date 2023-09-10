package com.example.finance.controller;

import com.example.finance.dto.AccountDto;
import com.example.finance.dto.CreateAccountDto;
import com.example.finance.response.AccountBalanceResponse;
import com.example.finance.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Create account")
    @PostMapping
    public Mono<AccountDto> createAccount(@RequestBody CreateAccountDto account) {
        log.info("Received account creation request");
        return accountService.createAccount(account);
    }

    @Operation(summary = "Get account balance by id")
    @GetMapping("{id}/balance")
    public Mono<AccountBalanceResponse> getBalance(@PathVariable Long id) {
        log.debug("Received get balance request for account {}", id);
        return accountService.getAccountBalance(id).map(balance -> AccountBalanceResponse.builder().balance(balance).build());
    }
    @Operation(summary = "Create transaction")
    @PostMapping("transaction")
    public Mono<String> transferFunds(@RequestParam Long senderId, @RequestParam Long receiverId, @RequestParam BigDecimal amount) {
        log.info("Received transaction request for amount {}", amount);
        return accountService.executeTransaction(senderId, receiverId, amount).then(Mono.fromCallable(() -> "Transaction proceeded successfully"));
    }
}
