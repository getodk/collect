package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.StringEndsWith.endsWith;

public class AdminSettingsPage extends Page<AdminSettingsPage> {

    @Override
    public AdminSettingsPage assertOnPage() {
        assertText(R.string.admin_preferences);
        return this;
    }

    public AdminSettingsPage openUserSettings() {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.user_settings));
        return this;
    }

    public GeneralSettingsPage clickGeneralSettings() {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.general_preferences));
        return new GeneralSettingsPage().assertOnPage();
    }


    public AdminSettingsPage clickFormEntrySettings() {
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.form_entry_setting));
        return this;
    }

    public AdminSettingsPage clickMovingBackwards() {
        clickOnString(R.string.moving_backwards_title);
        return this;
    }

    public AdminSettingsPage checkIfSaveFormOptionIsDisabled() {
        onView(withText(getTranslatedString(R.string.save_all_answers))).check(matches(not(isEnabled())));
        return this;
    }

    public AdminSettingsPage clickOnResetApplication() {
        clickOnString(R.string.reset_settings_dialog);
        return this;
    }

    public AdminSettingsPage uncheckServerOption() {
        clickOnString(R.string.server_settings_title);
        return this;
    }

    public QRCodePage clickConfigureQR() {
        clickOnString(R.string.configure_via_qr_code);
        return new QRCodePage().assertOnPage();
    }

    public AdminSettingsPage setProjectName(String projectName) {
        clickOnString(R.string.project_name);
        inputText(projectName);
        clickOKOnDialog();
        return this;
    }

    public AdminSettingsPage setProjectIcon(String projectIcon) {
        clickOnString(R.string.project_icon);
        onView(withClassName(endsWith("EditText"))).perform(replaceText(""));
        onView(withClassName(endsWith("EditText"))).perform(typeText(projectIcon));
        clickOKOnDialog();
        return this;
    }

    public AdminSettingsPage setProjectColor(String projectColor) {
        clickOnString(R.string.project_color);
        onView(withContentDescription(R.string.hex_color)).perform(replaceText(projectColor));
        clickOKOnDialog();
        return this;
    }

    public MainMenuPage deleteProject() {
        clickOnString(R.string.delete_project);
        clickOnString(R.string.delete_project_yes);
        return new MainMenuPage();
    }

    public FirstLaunchDialogPage deleteLastProject() {
        clickOnString(R.string.delete_project);
        clickOnString(R.string.delete_project_yes);
        return new FirstLaunchDialogPage();
    }
}
