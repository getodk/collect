package org.odk.collect.android.support.pages;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.support.ActivityHelpers;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsNot.not;

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
        Espresso.openActionBarOverflowOrOptionsMenu(ActivityHelpers.getActivity());
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

    public GeneralSettingsPage clickGeneralSettings() {
        clickOnString(R.string.general_preferences);
        return new GeneralSettingsPage(rule).assertOnPage();
    }

    public AdminSettingsPage clickAdminSettings() {
        clickOnString(R.string.admin_preferences);
        return new AdminSettingsPage(rule).assertOnPage();
    }

    public QRCodeTabsActivityPage clickConfigureQR() {
        clickOnString(R.string.configure_via_qr_code);
        return new QRCodeTabsActivityPage(rule).assertOnPage();
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

    public MainMenuPage assertStorageMigrationBannerIsDisplayed() {
        onView(withText(R.string.scoped_storage_banner_text)).check(matches(isDisplayed()));
        onView(withText(R.string.scoped_storage_learn_more)).check(matches(isDisplayed()));
        return this;
    }

    public MainMenuPage assertStorageMigrationCompletedBannerIsDisplayed() {
        onView(withText(R.string.storage_migration_completed)).check(matches(isDisplayed()));
        onView(withText(R.string.scoped_storage_dismiss)).check(matches(isDisplayed()));
        return this;
    }

    public MainMenuPage assertStorageMigrationCompletedBannerIsNotDisplayed() {
        onView(withId(R.id.storageMigrationBanner)).check(matches(not(isDisplayed())));
        return this;
    }

    public StorageMigrationDialogPage clickLearnMoreButton() {
        onView(withText(getTranslatedString(R.string.scoped_storage_learn_more))).perform(click());
        return new StorageMigrationDialogPage(rule).assertOnPage();
    }

    public MainMenuPage clickDismissButton() {
        onView(withText(getTranslatedString(R.string.scoped_storage_dismiss))).perform(click());
        return this;
    }

    public MainMenuPage recreateActivity() {
        getInstrumentation().runOnMainSync(() -> rule.getActivity().recreate());
        return this;
    }
}

