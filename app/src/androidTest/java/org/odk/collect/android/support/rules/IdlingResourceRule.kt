package org.odk.collect.android.support.rules

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class IdlingResourceRule(
    private val idlingResources: List<IdlingResource>
) : TestRule {

    constructor(idlingResource: IdlingResource) : this(listOf(idlingResource))

    override fun apply(base: Statement, description: Description): Statement =
        IdlingResourceStatement(idlingResources, base)

    private class IdlingResourceStatement(
        private val idlingResources: List<IdlingResource>,
        private val base: Statement
    ) : Statement() {

        override fun evaluate() {
            for (idlingResources in idlingResources) {
                IdlingRegistry.getInstance().register(idlingResources)
            }

            try {
                base.evaluate()
            } finally {
                for (idlingResources in idlingResources) {
                    IdlingRegistry.getInstance().unregister(idlingResources)
                }
            }
        }
    }
}
