package org.odk.collect.android.support;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class IdlingResourceRule implements TestRule {

    private final IdlingResource idlingResource;

    public IdlingResourceRule(IdlingResource idlingResource) {
        this.idlingResource = idlingResource;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new IdlingResourceStatement(idlingResource, base);
    }

    private static class IdlingResourceStatement extends Statement {

        private final IdlingResource idlingResource;
        private final Statement base;

        IdlingResourceStatement(IdlingResource idlingResource, Statement base) {
            this.idlingResource = idlingResource;
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            IdlingRegistry.getInstance().register(idlingResource);

            try {
                base.evaluate();
            } finally {
                IdlingRegistry.getInstance().unregister(idlingResource);
            }
        }
    }
}
