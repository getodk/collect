package org.odk.collect.android.support;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.test.FormLoadingUtils;

public class CopyFormRule implements TestRule {

    private final String fileName;

    public CopyFormRule(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new CopyFormStatement(fileName, base);
    }

    private class CopyFormStatement extends Statement {

        private final String fileName;
        private final Statement base;

        CopyFormStatement(String fileName, Statement base) {
            this.fileName = fileName;
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            FormLoadingUtils.copyFormToSdCard(fileName);
            base.evaluate();
        }
    }
}

