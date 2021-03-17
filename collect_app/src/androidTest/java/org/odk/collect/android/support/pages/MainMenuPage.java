package org.odk.collect.android.support.pages;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.support.ActivityHelpers;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.containsString;

public class MainMenuPage extends Page<MainMenuPage> {

    public MainMenuPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public MainMenuPage assertOnPage() {
        onView(withText(containsString(getTranslatedString(R.string.app_name)))).check(matches(isDisplayed()));
        return this;
    }

    public MainMenuPage clickOnMenu() {
        assertOnPage(); // Make sure we've waited for the application load correctly
        Espresso.openActionBarOverflowOrOptionsMenu(ActivityHelpers.getActivity());
        onView(withText(getTranslatedString(R.string.general_preferences))).check(matches(isDisplayed()));
        return this;
    }

    public FormEntryPage startBlankForm(String formName) {
        goToBlankForm(formName);
        return new FormEntryPage(formName, rule).assertOnPage();
    }

    public AddNewRepeatDialog startBlankFormWithRepeatGroup(String formName, String repeatName) {
        goToBlankForm(formName);
        return new AddNewRepeatDialog(repeatName, rule).assertOnPage();
    }

    public ErrorDialog startBlankFormWithError(String formName) {
        goToBlankForm(formName);
        return new ErrorDialog(rule).assertOnPage();
    }

    public OkDialog startBlankFormWithDialog(String formName) {
        goToBlankForm(formName);
        return new OkDialog(rule).assertOnPage();
    }

    public GeneralSettingsPage clickGeneralSettings() {
        clickOnString(R.string.general_preferences);
        return new GeneralSettingsPage(rule).assertOnPage();
    }

    public AdminSettingsPage clickAdminSettings() {
        clickOnString(R.string.admin_preferences);
        return new AdminSettingsPage(rule).assertOnPage();
    }

    public QRCodePage clickConfigureQR() {
        clickOnString(R.string.configure_via_qr_code);
        return new QRCodePage(rule).assertOnPage();
    }

    public QRCodePage clickConfigureQRWithAdminPassword(String password) {
        clickOnString(R.string.configure_via_qr_code);
        inputText(password);
        clickOKOnDialog();
        return new QRCodePage(rule).assertOnPage();
    }

    public FillBlankFormPage clickFillBlankForm() {
        onView(withId(R.id.enter_data)).perform(click());
        return new FillBlankFormPage(rule).assertOnPage();
    }

    private void goToBlankForm(String formName) {
        clickFillBlankForm();
        onData(withRowString(FormsColumns.DISPLAY_NAME, formName)).perform(click());
    }

    public EditSavedFormPage clickEditSavedForm() {
        onView(withId(R.id.review_data)).perform(click());
        return new EditSavedFormPage(rule).assertOnPage();
    }

    public EditSavedFormPage clickEditSavedForm(int formCount) {
        assertNumberOfEditableForms(formCount);
        return clickEditSavedForm();
    }

    public AboutPage clickAbout() {
        clickOnString(R.string.about_preferences);
        return new AboutPage(rule).assertOnPage();
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

    public MainMenuPage recreateActivity() {
        getInstrumentation().runOnMainSync(() -> rule.getActivity().recreate());
        return this;
    }

    public GetBlankFormPage clickGetBlankForm() {
        onView(withText(getTranslatedString(R.string.get_forms))).perform(scrollTo(), click());
        return new GetBlankFormPage(rule).assertOnPage();
    }

    public SendFinalizedFormPage clickSendFinalizedForm(int formCount) {
        onView(withText(getTranslatedString(R.string.send_data_button, formCount))).perform(click());
        return new SendFinalizedFormPage(rule);
    }

    public MainMenuPage setServer(String url) {
        return clickOnMenu()
                .clickGeneralSettings()
                .clickServerSettings()
                .clickOnURL()
                .inputText(url)
                .clickOKOnDialog()
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule));
    }

    public MainMenuPage enableManualUpdates() {
        return clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.manual)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule));
    }

    public MainMenuPage enablePreviouslyDownloadedOnlyUpdates() {
        return clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.previously_downloaded_only)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule));
    }

    public MainMenuPage enableMatchExactly() {
        return clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.match_exactly)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule));
    }

    public MainMenuPage enableAutoSend() {
        return clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickOnString(R.string.autosend)
                .clickOnString(R.string.wifi_cellular_autosend)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule));
    }

    public MainMenuPage setGoogleAccount(String account) {
        Intent data = new Intent();
        data.putExtra(AccountManager.KEY_ACCOUNT_NAME, account);
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, data);
        intending(hasAction("PICK_GOOGLE_ACCOUNT")).respondWith(activityResult);

        return clickOnMenu()
                .clickGeneralSettings()
                .clickServerSettings()
                .clickOnServerType()
                .clickOnString(R.string.server_platform_google_sheets)
                .clickOnString(R.string.selected_google_account_text)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule));
    }

    public ServerAuthDialog clickGetBlankFormWithAuthenticationError() {
        onView(withText(getTranslatedString(R.string.get_forms))).perform(scrollTo(), click());
        return new ServerAuthDialog(rule).assertOnPage();
    }

    public OkDialog clickGetBlankFormWithError() {
        onView(withText(getTranslatedString(R.string.get_forms))).perform(scrollTo(), click());
        return new OkDialog(rule).assertOnPage();
    }

    public ViewSentFormPage clickViewSentForm(int formCount) {
        onView(withText(getTranslatedString(R.string.view_sent_forms_button, formCount))).perform(click());
        return new ViewSentFormPage(rule).assertOnPage();
    }

    public DeleteSavedFormPage clickDeleteSavedForm() {
        onView(withText(getTranslatedString(R.string.manage_files))).check(matches(isClickable()));
        onView(withText(getTranslatedString(R.string.manage_files))).perform(scrollTo(), click());
        return new DeleteSavedFormPage(rule).assertOnPage();
    }
}

