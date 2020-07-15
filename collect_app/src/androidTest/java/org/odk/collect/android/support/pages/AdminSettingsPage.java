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
        clickOnString(R.string.user_settings);
        return this;
    }

    public AdminSettingsPage uncheckAllUserSettings() {
        clickOnString(R.string.server);
        clickOnString(R.string.app_theme);
        clickOnString(R.string.language);
        clickOnString(R.string.font_size);
        clickOnString(R.string.navigation);
        clickOnString(R.string.show_splash_title);
        clickOnString(R.string.maps);
        clickOnString(R.string.periodic_form_updates_check_title);
        clickOnString(R.string.automatic_download);
        scrollToViewAndClickText(getTranslatedString(R.string.hide_old_form_versions_setting_title));
        scrollToViewAndClickText(getTranslatedString(R.string.autosend));
        scrollToViewAndClickText(getTranslatedString(R.string.delete_after_send));
        scrollToViewAndClickText(getTranslatedString(R.string.default_completed));
        scrollToViewAndClickText(getTranslatedString(R.string.constraint_behavior_title));
        scrollToViewAndClickText(getTranslatedString(R.string.high_resolution_title));
        scrollToViewAndClickText(getTranslatedString(R.string.image_size_title));
        scrollToViewAndClickText(getTranslatedString(R.string.guidance_hint_title));
        scrollToViewAndClickText(getTranslatedString(R.string.instance_sync));
        scrollToViewAndClickText(getTranslatedString(R.string.form_metadata));
        scrollToViewAndClickText(getTranslatedString(R.string.analytics));
        return this;
    }

    public GeneralSettingsPage clickGeneralSettings() {
        scrollToViewAndClickText(getTranslatedString(R.string.general_preferences));
        return new GeneralSettingsPage(rule).assertOnPage();
    }

    public AdminSettingsPage uncheckUserSettings(int id) {
        scrollToViewAndClickText(getTranslatedString(id));
        return this;
    }

    public AdminSettingsPage clickFormEntrySettings() {
        scrollToViewAndClickText(getTranslatedString(R.string.form_entry_setting));
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
        clickOnString(R.string.server);
        return this;
    }

}
