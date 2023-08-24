package com.example.finmid.integration

import com.example.finmid.dto.AccountDto
import com.example.finmid.entinty.AccountEntity
import com.example.finmid.repository.AccountRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureWebTestClient
open class DefaultIntegrationTest {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var webTestClient: WebTestClient

    @AfterEach
    fun clean() {
        accountRepository.deleteAll().doOnSuccess { log.info("After test: removed test data") }.block()
    }

    fun prepareTestAccount(accountEntity: AccountEntity): Long {
        return accountRepository.save(accountEntity).map { it.id }.block()!!
    }

    fun assertAccountExist(account: AccountDto) {
        val accountEntity = accountRepository.findById(account.id).block()
        assertThat(accountEntity).isNotNull
        assertThat(account.balance).isEqualByComparingTo(accountEntity?.balance)
        assertThat(account.createDate).isEqualToIgnoringNanos(accountEntity?.createDate)
        assertThat(account.modifyDate).isEqualToIgnoringNanos(accountEntity?.modifyDate)
    }

    fun assertAccountBalance(accountId: Long, balance: BigDecimal) {
        val accountEntity = accountRepository.findById(accountId).block()
        assertThat(accountEntity).isNotNull
        assertThat(accountEntity?.balance).isEqualByComparingTo(balance)
    }
}