package org.odk.collect.android.support.pages;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.PreferenceMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
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
        clickOnString(R.string.hide_old_form_versions_setting_title);
        clickOnString(R.string.autosend);
        clickOnString(R.string.delete_after_send);
        clickOnString(R.string.default_completed);
        clickOnString(R.string.constraint_behavior_title);
        clickOnString(R.string.high_resolution_title);
        clickOnString(R.string.image_size_title);
        clickOnString(R.string.guidance_hint_title);
        clickOnString(R.string.instance_sync);
        clickOnString(R.string.form_metadata);
        clickOnString(R.string.analytics);
        return this;
    }

    public GeneralSettingsPage clickGeneralSettings() {
        onData(PreferenceMatchers.withKey("odk_preferences")).perform(click());
        return new GeneralSettingsPage(rule).assertOnPage();
    }

    public AdminSettingsPage uncheckUserSettings(String setting) {
        onData(PreferenceMatchers.withKey(setting)).perform(ViewActions.scrollTo());
        onData(PreferenceMatchers.withKey(setting)).perform(click());
        return this;
    }

    public AdminSettingsPage clickFormEntrySettings() {
        clickOnString(R.string.form_entry_setting);
        return this;
    }

    public AdminSettingsPage clickMovingBackwards() {
        clickOnString(R.string.moving_backwards_title);
        return this;
    }

    public AdminSettingsPage checkIfSaveFormOptionIsDisabled() {
        onData(PreferenceMatchers.withKey("save_mid")).check(matches(not(isEnabled())));
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
