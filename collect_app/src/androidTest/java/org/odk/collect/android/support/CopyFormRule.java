package org.odk.collect.android.support;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.projects.Project;

import java.util.List;

/**
 * @deprecated using this forces us to set the app as if it was being "upgraded"
 * as otherwise we can't copy forms before the app has been launched.
 */
@Deprecated
public class CopyFormRule implements TestRule {

    public static boolean projectCreated;

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
            // Set up demo project if none exists
            AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Application>getApplicationContext());
            try {
                component.currentProjectProvider().getCurrentProject();
            } catch (IllegalStateException e) {
                component.projectsRepository().save(Project.Companion.getDEMO_PROJECT());
                component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID);
                projectCreated = true;
            }

            AdbFormLoadingUtils.copyFormToStorage(fileName, mediaFilePaths, copyToDatabase, fileName, "Demo project");
            base.evaluate();
        }
    }
}

