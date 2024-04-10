package org.odk.collect.android.support.rules

import android.os.Build
import org.junit.Assert.assertTrue
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.odk.collect.android.support.pages.NotificationDrawer

class NotificationDrawerRule : TestRule {
    private val notificationDrawer = NotificationDrawer()

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                assertTrue(
                    "${this.javaClass.simpleName} does not support this API level!",
                    SUPPORTED_SDKS.contains(Build.VERSION.SDK_INT)
                )

                try {
                    base.evaluate()
                } finally {
                    notificationDrawer.teardown()
                }
            }
        }
    }

    fun open(): NotificationDrawer {
        return notificationDrawer.open()
    }

    companion object {
        private val SUPPORTED_SDKS = listOf(30, 34)
    }
}
