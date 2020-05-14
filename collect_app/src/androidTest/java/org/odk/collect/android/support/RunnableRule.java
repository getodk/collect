package org.odk.collect.android.support;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class RunnableRule implements TestRule {

    private final Runnable runnable;

    public RunnableRule(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                runnable.run();
                base.evaluate();
            }
        };
    }
}
