package com.example.finance.integration

import com.example.finance.dto.AccountDto
import com.example.finance.dto.CreateAccountDto
import com.example.finance.entinty.AccountEntity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class AccountControllerIntegrationTest : DefaultIntegrationTest() {

    @Test
    fun `should create new account`() {
        val createAccountDto = CreateAccountDto.builder().balance(BigDecimal.valueOf(100)).build()

        val account = webTestClient.post()
                .uri("/api/v1/account")
                .bodyValue(createAccountDto)
                .exchange()
                .expectStatus().isOk
                .expectBody(AccountDto::class.java)
                .returnResult().responseBody!!

        assertAccountExist(account)
    }

    @Test
    fun `should return account balance`() {
        val testAccount = AccountEntity.builder().balance(BigDecimal.valueOf(150)).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).build()

        val id = prepareTestAccount(testAccount)

        webTestClient.get()
                .uri("/api/v1/account/$id/balance")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json("{\"balance\":150.0}")
    }

    @Test
    fun `should successfully transfer money`() {
        val senderAccount = AccountEntity.builder().balance(BigDecimal.valueOf(150)).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).build()
        val receiverAccount = AccountEntity.builder().balance(BigDecimal.valueOf(50)).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).build()

        val senderId = prepareTestAccount(senderAccount)
        val receiverId = prepareTestAccount(receiverAccount)

        assertAccountBalance(senderId, BigDecimal.valueOf(150))
        assertAccountBalance(receiverId, BigDecimal.valueOf(50))

        val responseBody = webTestClient.post()
                .uri { builder ->
                    builder
                            .path("/api/v1/account/transaction")
                            .queryParam("senderId", senderId)
                            .queryParam("receiverId", receiverId)
                            .queryParam("amount", 50)
                            .build()
                }
                .exchange()
                .expectStatus().isOk
                .expectBody(String::class.java)
                .returnResult().responseBody

        assertThat(responseBody).isEqualTo("Transaction proceeded successfully")

        assertAccountBalance(senderId, BigDecimal.valueOf(100))
        assertAccountBalance(receiverId, BigDecimal.valueOf(100))
    }

    @Test
    fun `should fail on same account transfer`() {
        val account = AccountEntity.builder().balance(BigDecimal.valueOf(150)).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).build()

        val accountId = prepareTestAccount(account)

        val responseBody = webTestClient.post()
                .uri { builder ->
                    builder
                            .path("/api/v1/account/transaction")
                            .queryParam("senderId", accountId)
                            .queryParam("receiverId", accountId)
                            .queryParam("amount", 50)
                            .build()
                }
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult().responseBody

        assertThat(responseBody).isEqualTo("Same sender and receiver, account id: $accountId")
    }

    @Test
    fun `should fail on insufficient money transfer`() {
        val senderAccount = AccountEntity.builder().balance(BigDecimal.valueOf(150)).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).build()
        val receiverAccount = AccountEntity.builder().balance(BigDecimal.valueOf(50)).createDate(LocalDateTime.now()).modifyDate(LocalDateTime.now()).build()

        val senderId = prepareTestAccount(senderAccount)
        val receiverId = prepareTestAccount(receiverAccount)

        val responseBody = webTestClient.post()
                .uri { builder ->
                    builder
                            .path("/api/v1/account/transaction")
                            .queryParam("senderId", senderId)
                            .queryParam("receiverId", receiverId)
                            .queryParam("amount", 300)
                            .build()
                }
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult().responseBody

        assertThat(responseBody).isEqualTo("Insufficient funds for account: $senderId")
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
            repeat(1001) {
                transfer(account1Id, account2Id, BigDecimal.valueOf(100))
            }
            repeat(1000) {
                transfer(account2Id, account1Id, BigDecimal.valueOf(100))
            }
        }

        assertAccountBalance(account1Id, BigDecimal.valueOf(199_900))
        assertAccountBalance(account2Id, BigDecimal.valueOf(150_100))
    }

    private suspend fun transfer(senderId: Long, receiverId: Long, amount: BigDecimal) = coroutineScope {
        launch {
            val responseBody = webTestClient.post()
                    .uri { builder ->
                        builder
                                .path("/api/v1/account/transaction")
                                .queryParam("senderId", senderId)
                                .queryParam("receiverId", receiverId)
                                .queryParam("amount", amount)
                                .build()
                    }
                    .exchange()
                    .expectStatus().isOk
                    .expectBody(String::class.java)
                    .returnResult().responseBody

            assertThat(responseBody).isEqualTo("Transaction proceeded successfully")
        }
    }
}