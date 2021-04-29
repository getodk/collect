package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

public class AdminSettingsPage extends Page<AdminSettingsPage> {

    public AdminSettingsPage(ActivityTestRule rule) {
        super(rule);
    }

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
        return new GeneralSettingsPage(rule).assertOnPage();
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
        return new QRCodePage(rule).assertOnPage();
    }

    public AdminSettingsPage setProjectName(String projectName) {
        clickOnString(R.string.project_name);
        inputText(projectName);
        clickOKOnDialog();
        return this;
    }

    public AdminSettingsPage setProjectIcon(String projectIcon) {
        clickOnString(R.string.project_icon);
        inputText(projectIcon);
        clickOKOnDialog();
        return this;
    }

    public AdminSettingsPage setProjectColor(String projectColor) {
        clickOnString(R.string.project_color);
        inputText(projectColor);
        clickOKOnDialog();
        return this;
    }
}
