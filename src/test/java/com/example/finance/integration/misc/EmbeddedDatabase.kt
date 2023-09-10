package com.example.finance.integration.misc

import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.testcontainers.containers.PostgreSQLContainer

@Slf4j
class EmbeddedDatabase : SmartLifecycle {

    private val log = LoggerFactory.getLogger(this::class.java)

    private val postgresContainer = PostgreSQLContainer("postgres:latest")

    override fun start() {
        log.info("Starting Postgres container")
        postgresContainer.start()
        System.setProperty("POSTGRES_PORT", postgresContainer.firstMappedPort.toString())
    }

    override fun stop() {
        log.info("Stopping Postgres container")
        postgresContainer.stop()
    }

    override fun isRunning(): Boolean {
        return postgresContainer.isRunning
    }
}