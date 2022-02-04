package org.odk.collect.android.support.rules

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RunnableRule(
    private val runnable: Runnable
) : TestRule {

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                runnable.run()
                base.evaluate()
            }
        }
}
