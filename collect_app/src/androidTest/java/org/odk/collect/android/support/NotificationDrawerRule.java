package org.odk.collect.android.support;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.support.pages.NotificationDrawer;

public class NotificationDrawerRule implements TestRule {
    
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } finally {
                    NotificationDrawer.teardown();
                }
            }
        };
    }

    public NotificationDrawer open() {
        return NotificationDrawer.open();
    }
}
