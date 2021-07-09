package org.odk.collect.android.support.pages;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

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
        scrollToRecyclerViewItemAndClickText(getTranslatedString(R.string.project_settings));
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

    public AdminSettingsPage uncheckServerOption() {
        clickOnString(R.string.server_settings_title);
        return this;
    }
}
