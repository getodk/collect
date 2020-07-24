package org.odk.collect.android.support;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.List;

import static java.util.Collections.singletonList;

public class IdlingResourceRule implements TestRule {

    private final List<IdlingResource> idlingResources;

    public IdlingResourceRule(IdlingResource idlingResource) {
        this.idlingResources = singletonList(idlingResource);
    }

    public IdlingResourceRule(List<IdlingResource> idlingResources) {
        this.idlingResources = idlingResources;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new IdlingResourceStatement(idlingResources, base);
    }

    private static class IdlingResourceStatement extends Statement {

        private final List<IdlingResource> idlingResources;
        private final Statement base;

        IdlingResourceStatement(List<IdlingResource> idlingResources, Statement base) {
            this.idlingResources = idlingResources;
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            for (IdlingResource idlingResources : idlingResources) {
                IdlingRegistry.getInstance().register(idlingResources);
            }

            try {
                base.evaluate();
            } finally {
                for (IdlingResource idlingResources : idlingResources) {
                    IdlingRegistry.getInstance().unregister(idlingResources);
                }
            }
        }
    }
}
