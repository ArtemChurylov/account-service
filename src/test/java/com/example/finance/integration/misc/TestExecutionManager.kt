package com.example.finance.integration.misc

import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestPlan

class TestExecutionManager : TestExecutionListener {

    private val embeddedDatabase = EmbeddedDatabase()
    override fun testPlanExecutionStarted(testPlan: TestPlan?) {
        embeddedDatabase.start()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan?) {
        embeddedDatabase.stop()
    }
}