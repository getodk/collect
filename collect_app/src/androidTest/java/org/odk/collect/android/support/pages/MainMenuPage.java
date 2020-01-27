package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.support.ActivityHelpers;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class MainMenuPage extends Page<MainMenuPage> {

    public MainMenuPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public MainMenuPage assertOnPage() {
        checkIsStringDisplayed(R.string.main_menu);
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
}

