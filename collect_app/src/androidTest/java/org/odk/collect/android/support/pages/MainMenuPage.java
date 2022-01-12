package org.odk.collect.android.support.pages;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import org.hamcrest.core.StringContains;
import org.odk.collect.android.R;
import org.odk.collect.android.support.WaitFor;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

public class MainMenuPage extends Page<MainMenuPage> {

    @Override
    public MainMenuPage assertOnPage() {
        return WaitFor.waitFor(() -> {
            onView(withText(StringContains.containsString(getTranslatedString(R.string.collect_app_name)))).perform(scrollTo()).check(matches(isDisplayed()));
            return this;
        });
    }

    public ProjectSettingsDialogPage openProjectSettingsDialog() {
        assertOnPage(); // Make sure we've waited for the application load correctly

        onView(withId(R.id.projects)).perform(click());
        // It seems there is some lag here sometimes
        return WaitFor.waitFor(() -> {
            // It seems there is some lag here sometimes
            return new ProjectSettingsDialogPage().assertOnPage();
        });
    }

    public FormEntryPage startBlankForm(String formName) {
        goToBlankForm(formName);
        return new FormEntryPage(formName).assertOnPage();
    }

    public AddNewRepeatDialog startBlankFormWithRepeatGroup(String formName, String repeatName) {
        goToBlankForm(formName);
        return new AddNewRepeatDialog(repeatName).assertOnPage();
    }

    public ErrorDialog startBlankFormWithError(String formName) {
        goToBlankForm(formName);
        return new ErrorDialog().assertOnPage();
    }

    public OkDialog startBlankFormWithDialog(String formName) {
        goToBlankForm(formName);
        return new OkDialog().assertOnPage();
    }

    public FillBlankFormPage clickFillBlankForm() {
        onView(withId(R.id.enter_data)).perform(click());
        return new FillBlankFormPage().assertOnPage();
    }

    private void goToBlankForm(String formName) {
        clickFillBlankForm().clickOnForm(formName);
    }

    public EditSavedFormPage clickEditSavedForm() {
        onView(withId(R.id.review_data)).perform(click());
        return new EditSavedFormPage().assertOnPage();
    }

    public EditSavedFormPage clickEditSavedForm(int formCount) {
        assertNumberOfEditableForms(formCount);
        return clickEditSavedForm();
    }

    public MainMenuPage assertNumberOfFinalizedForms(int number) {
        if (number == 0) {
            onView(withText(getTranslatedString(R.string.send_data))).check(matches(isDisplayed()));
        } else {
            onView(withText(getTranslatedString(R.string.send_data_button, String.valueOf(number)))).check(matches(isDisplayed()));
        }
        return this;
    }

    public MainMenuPage assertNumberOfEditableForms(int number) {
        if (number == 0) {
            onView(withText(getTranslatedString(R.string.review_data))).check(matches(isDisplayed()));
        } else {
            onView(withText(getTranslatedString(R.string.review_data_button, String.valueOf(number)))).check(matches(isDisplayed()));
        }

        return this;
    }

    public GetBlankFormPage clickGetBlankForm() {
        onView(withText(getTranslatedString(R.string.get_forms))).perform(scrollTo(), click());
        return new GetBlankFormPage().assertOnPage();
    }

    public SendFinalizedFormPage clickSendFinalizedForm(int formCount) {
        onView(withText(getTranslatedString(R.string.send_data_button, formCount))).perform(click());
        return new SendFinalizedFormPage();
    }

    public MainMenuPage setServer(String url) {
        return openProjectSettingsDialog()
                .clickSettings()
                .clickServerSettings()
                .clickOnURL()
                .inputText(url)
                .clickOKOnDialog()
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage enableManualUpdates() {
        return openProjectSettingsDialog()
                .clickSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.manual)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage enablePreviouslyDownloadedOnlyUpdates() {
        return openProjectSettingsDialog()
                .clickSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.previously_downloaded_only)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage enablePreviouslyDownloadedOnlyUpdatesWithAutomaticDownload() {
        return openProjectSettingsDialog()
                .clickSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.previously_downloaded_only)
                .clickOnString(R.string.automatic_download)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage enableMatchExactly() {
        return openProjectSettingsDialog()
                .clickSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.match_exactly)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage enableAutoSend() {
        return openProjectSettingsDialog()
                .clickSettings()
                .clickFormManagement()
                .clickOnString(R.string.autosend)
                .clickOnString(R.string.wifi_cellular_autosend)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage setGoogleAccount(String account) {
        Intent data = new Intent();
        data.putExtra(AccountManager.KEY_ACCOUNT_NAME, account);
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, data);
        intending(hasAction("com.google.android.gms.common.account.CHOOSE_ACCOUNT")).respondWith(activityResult);

        return openProjectSettingsDialog()
                .clickSettings()
                .clickServerSettings()
                .clickOnServerType()
                .clickOnString(R.string.server_platform_google_sheets)
                .clickOnString(R.string.selected_google_account_text)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage addAndSwitchToProject(String serverUrl) {
        return openProjectSettingsDialog()
                .clickAddProject()
                .switchToManualMode()
                .inputUrl(serverUrl)
                .addProject();
    }

    public ServerAuthDialog clickGetBlankFormWithAuthenticationError() {
        onView(withText(getTranslatedString(R.string.get_forms))).perform(scrollTo(), click());
        return new ServerAuthDialog().assertOnPage();
    }

    public OkDialog clickGetBlankFormWithError() {
        onView(withText(getTranslatedString(R.string.get_forms))).perform(scrollTo(), click());
        return new OkDialog().assertOnPage();
    }

    public ViewSentFormPage clickViewSentForm(int formCount) {
        String text = formCount < 1
                ? getTranslatedString(R.string.view_sent_forms)
                : getTranslatedString(R.string.view_sent_forms_button, formCount);
        onView(withText(text)).perform(click());
        return new ViewSentFormPage().assertOnPage();
    }

    public DeleteSavedFormPage clickDeleteSavedForm() {
        onView(withText(getTranslatedString(R.string.manage_files))).check(matches(isClickable()));
        onView(withText(getTranslatedString(R.string.manage_files))).perform(scrollTo(), click());
        return new DeleteSavedFormPage().assertOnPage();
    }

    public MainMenuPage assertProjectIcon(String projectIcon) {
        onView(allOf(hasDescendant(withText(projectIcon)), withId(R.id.projects))).check(matches(isDisplayed()));
        return this;
    }

    public MainMenuPage copyAndSyncForm(String formFilename) {
        return copyForm(formFilename)
                .clickFillBlankForm()
                .pressBack(new MainMenuPage());
    }
}

