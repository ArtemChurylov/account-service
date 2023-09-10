package com.example.finance.integration;

import com.example.finance.entinty.AccountEntity
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.dao.OptimisticLockingFailureException
import java.math.BigDecimal
import java.time.LocalDateTime

class OptimisticLockingIntegrationTest : DefaultIntegrationTest() {

    @Test
    fun `should throw optimistic lock exception`() {
        val account = AccountEntity.builder().balance(BigDecimal.valueOf(150)).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).build()
        val id = prepareTestAccount(account)

        val accountFromDB = accountRepository.findById(id).block()!!
        accountFromDB.balance = BigDecimal.valueOf(250)

        accountRepository.save(accountFromDB).block()

        account.balance = BigDecimal.valueOf(300)
        assertThatThrownBy { accountRepository.save(account).block() }.isInstanceOf(OptimisticLockingFailureException::class.java)
    }
}
