package org.odk.collect.android.support;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.test.FormLoadingUtils;

import java.util.List;

public class CopyFormRule implements TestRule {

    private final String fileName;
    private final List<String> mediaFilePaths;
    private final boolean copyToDatabase;

    public CopyFormRule(String fileName) {
        this(fileName, null, false);
    }

    public CopyFormRule(String fileName, List<String> mediaFilenames) {
        this(fileName, mediaFilenames, false);
    }

    public CopyFormRule(String fileName, boolean copyToDatabase) {
        this(fileName, null, copyToDatabase);
    }

    public CopyFormRule(String fileName, List<String> mediaFilePaths, boolean copyToDatabase) {
        this.fileName = fileName;
        this.mediaFilePaths = mediaFilePaths;
        this.copyToDatabase = copyToDatabase;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new CopyFormStatement(fileName, mediaFilePaths, copyToDatabase, base);
    }

    private class CopyFormStatement extends Statement {

        private final String fileName;
        private final List<String> mediaFilePaths;

        private final boolean copyToDatabase;
        private final Statement base;

        CopyFormStatement(String fileName, List<String> mediaFilePaths, boolean copyToDatabase, Statement base) {
            this.fileName = fileName;
            this.mediaFilePaths = mediaFilePaths;
            this.copyToDatabase = copyToDatabase;
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            FormLoadingUtils.copyFormToStorage(fileName, mediaFilePaths, copyToDatabase);
            base.evaluate();
        }
    }
}

