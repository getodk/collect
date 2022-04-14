package org.odk.collect.android.support.rules

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import timber.log.Timber

class RetryOnDeviceErrorRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                try {
                    base.evaluate()
                } catch (e: Throwable) {
                    if (e::class.simpleName == "RootViewWithoutFocusException") {
                        Timber.e("RetryOnDeviceErrorRule: Retrying due to RootViewWithoutFocusException!")
                        base.evaluate()
                    } else {
                        throw e
                    }
                }
            }
        }
    }
}
