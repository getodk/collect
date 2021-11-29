package org.odk.collect.testshared

import androidx.test.espresso.intent.Intents
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RecordedIntentsRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                try {
                    Intents.init()
                    base.evaluate()
                } finally {
                    Intents.release()
                }
            }
        }
    }
}
