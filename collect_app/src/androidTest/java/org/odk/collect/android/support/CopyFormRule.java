package org.odk.collect.android.support;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.test.FormLoadingUtils;

import java.util.List;

public class CopyFormRule implements TestRule {

    private final String fileName;
    private final String formAssetPath;
    private final List<String> mediaFilenames;

    public CopyFormRule(String fileName) {
        this(fileName, null, null);
    }

    public CopyFormRule(String fileName, String formAssetPath, List<String> mediaFilenames) {
        this.fileName = fileName;
        this.formAssetPath = formAssetPath;
        this.mediaFilenames = mediaFilenames;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new CopyFormStatement(fileName, formAssetPath, mediaFilenames, base);
    }

    private class CopyFormStatement extends Statement {

        private final String fileName;
        private final String formAssetPath;
        private final List<String> mediaFilenames;

        private final Statement base;

        CopyFormStatement(String fileName, String formAssetPath, List<String> mediaFilenames, Statement base) {
            this.fileName = fileName;
            this.formAssetPath = formAssetPath;
            this.mediaFilenames = mediaFilenames;
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            if (formAssetPath != null && mediaFilenames != null) {
                FormLoadingUtils.copyFormToSdCard(fileName, formAssetPath, mediaFilenames);
            } else {
                FormLoadingUtils.copyFormToSdCard(fileName);
            }

            base.evaluate();
        }
    }
}

