package org.odk.collect.android.rules

import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.IOException
import java.util.ArrayList

class MockWebServerRule : TestRule {

    private val mockWebServers: MutableList<MockWebServer> = ArrayList()

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                try {
                    base.evaluate()
                } finally {
                    teardown()
                }
            }
        }

    @Throws(Exception::class)
    fun start(): MockWebServer {
        val mockWebServer = MockWebServer()
        mockWebServers.add(mockWebServer)

        mockWebServer.start()
        return mockWebServer
    }

    @Throws(IOException::class)
    fun teardown() {
        for (mockWebServer in mockWebServers) {
            mockWebServer.close()
        }
    }
}
