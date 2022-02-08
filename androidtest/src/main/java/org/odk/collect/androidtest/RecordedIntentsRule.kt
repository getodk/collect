package org.odk.collect.androidtest

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.rule.ActivityTestRule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Like [IntentsTestRule] but doesn't extend [ActivityTestRule] (and therefore works with
 * [ActivityScenario]/[FragmentScenario]
 */
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
