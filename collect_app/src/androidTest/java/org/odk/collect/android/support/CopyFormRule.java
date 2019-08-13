package org.odk.collect.android.support;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.test.FormLoadingUtils;

import java.util.List;

public class CopyFormRule implements TestRule {

    private final String fileName;
    private final List<String> mediaFilenames;

    public CopyFormRule(String fileName) {
        this(fileName, null);
    }

    public CopyFormRule(String fileName, List<String> mediaFilenames) {
        this.fileName = fileName;
        this.mediaFilenames = mediaFilenames;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new CopyFormStatement(fileName, mediaFilenames, base);
    }

    private class CopyFormStatement extends Statement {

        private final String fileName;
        private final List<String> mediaFilenames;

        private final Statement base;

        CopyFormStatement(String fileName, List<String> mediaFilenames, Statement base) {
            this.fileName = fileName;
            this.mediaFilenames = mediaFilenames;
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            FormLoadingUtils.copyFormToSdCard(fileName, mediaFilenames);
            base.evaluate();
        }
    }
}

