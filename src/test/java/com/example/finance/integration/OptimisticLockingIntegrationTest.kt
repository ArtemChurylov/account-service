package com.example.finance.integration;

import com.example.finance.entinty.AccountEntity
import com.example.finance.service.AccountService
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.random.Random

class OptimisticLockingIntegrationTest : DefaultIntegrationTest() {

    @Autowired
    lateinit var accountService: AccountService

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

    @Test
    fun `should successfully handle concurrent transactions`() {
        val account1 = AccountEntity.builder().balance(BigDecimal.valueOf(200_000)).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).build()
        val account2 = AccountEntity.builder().balance(BigDecimal.valueOf(150_000)).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).build()

        val account1Id = prepareTestAccount(account1)
        val account2Id = prepareTestAccount(account2)

        assertAccountBalance(account1Id, BigDecimal.valueOf(200_000))
        assertAccountBalance(account2Id, BigDecimal.valueOf(150_000))

        runBlocking {
            repeat(1000) {
                launch(Dispatchers.Unconfined) {
                    delay(Random.nextLong(100))
                    accountService.executeTransaction(account1Id, account2Id, BigDecimal.valueOf(100)).block()
                }
            }
        }

        assertAccountBalance(account1Id, BigDecimal.valueOf(100_000))
        assertAccountBalance(account2Id, BigDecimal.valueOf(250_000))
    }
}
