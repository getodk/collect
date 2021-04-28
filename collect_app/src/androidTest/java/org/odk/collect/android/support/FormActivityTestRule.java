package org.odk.collect.android.support;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import static org.odk.collect.android.activities.FormEntryActivity.EXTRA_TESTING_PATH;

public class FormActivityTestRule implements TestRule {

    private final String formFilename;

    public FormActivityTestRule(String formFilename) {
        this.formFilename = formFilename;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                ActivityScenario.launch(getActivityIntent());
                base.evaluate();
            }
        };
    }

    private Intent getActivityIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FormEntryActivity.class);
        intent.putExtra(EXTRA_TESTING_PATH, new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS) + "/" + formFilename);

        return intent;
    }
}
