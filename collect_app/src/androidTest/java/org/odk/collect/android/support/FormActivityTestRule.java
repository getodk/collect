package org.odk.collect.android.support;

import android.app.Application;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.external.FormsContract;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.pages.FirstLaunchPage;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.forms.Form;

import java.util.List;

public class FormActivityTestRule implements TestRule {

    private final String formFilename;
    private final String formName;
    private final List<String> mediaFilePaths;
    private FormEntryPage formEntryPage;

    public FormActivityTestRule(String formFilename, String formName) {
        this.formFilename = formFilename;
        this.formName = formName;
        this.mediaFilePaths = null;
    }

    public FormActivityTestRule(String formFilename, String formName, List<String> mediaFilePaths) {
        this.formFilename = formFilename;
        this.formName = formName;
        this.mediaFilePaths = mediaFilePaths;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                initialCopyForm();
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

    private void initialCopyForm() {
        ActivityScenario.launch(SplashScreenActivity.class);
        FirstLaunchPage firstLaunchPage = new FirstLaunchPage().assertOnPage();
        firstLaunchPage.clickTryCollect().copyForm(formFilename, mediaFilePaths, true);
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
