package org.odk.collect.android.support;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.storage.StorageStateProvider;

public class StorageMigrationNotPerformedRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new StorageMigrationNotPerformedRuleStatement(base);
    }

    private class StorageMigrationNotPerformedRuleStatement extends Statement {

        private final Statement base;

        StorageMigrationNotPerformedRuleStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            new StorageStateProvider().disableUsingScopedStorage();
            base.evaluate();
        }
    }
}
