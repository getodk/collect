package org.odk.collect.android.support;

import android.app.Application;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.external.FormsContract;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.forms.Form;

public class FormActivityTestRule implements TestRule {

    private final String formFilename;
    private final String formName;
    private FormEntryPage formEntryPage;

    public FormActivityTestRule(String formFilename, String formName) {
        this.formFilename = formFilename;
        this.formName = formName;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                ActivityScenario.launch(getActivityIntent());
                formEntryPage = new FormEntryPage(formName);
                formEntryPage.assertOnPage();

                base.evaluate();
            }
        };
    }

    public FormEntryPage startInFormEntry() {
        return formEntryPage;
    }

    private Intent getActivityIntent() {
        Application application = ApplicationProvider.getApplicationContext();

        String formPath = DaggerUtils.getComponent(application).storagePathProvider().getOdkDirPath(StorageSubdirectory.FORMS) + "/" + formFilename;
        Form form = DaggerUtils.getComponent(application).formsRepositoryProvider().get().getOneByPath(formPath);
        String projectId = DaggerUtils.getComponent(application).currentProjectProvider().getCurrentProject().getUuid();

        Intent intent = new Intent(application, FormEntryActivity.class);
        intent.setData(FormsContract.getUri(projectId, form.getDbId()));
        return intent;
    }
}
