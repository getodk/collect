package org.odk.collect.android.espressoutils.pages;

import org.odk.collect.android.R;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.support.ActivityHelpers;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.CursorMatchers.withRowString;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

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

    public AddNewGroupDialog startBlankFormWithRepeatGroup(String formName) {
        goToBlankForm(formName);

        return new AddNewGroupDialog(rule).assertOnPage();
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
        onView(withId(R.id.enter_data)).perform(click());
        onData(withRowString(FormsColumns.DISPLAY_NAME, formName)).perform(click());
    }

    public EditSavedFormPage clickEditSavedForm() {
        onView(withId(R.id.review_data)).perform(click());
        return new EditSavedFormPage(rule).assertOnPage();
    }
}

