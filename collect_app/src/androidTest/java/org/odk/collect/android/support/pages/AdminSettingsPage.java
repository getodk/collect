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
        checkIsStringDisplayed(R.string.admin_preferences);
        return this;
    }

    public AdminSettingsPage openUserSettings() {
        onData(PreferenceMatchers.withKey("user_settings")).perform(click());
        return this;
    }

    public AdminSettingsPage uncheckAllUserSettings() {
        onData(PreferenceMatchers.withKey("change_server")).perform(click());
        onData(PreferenceMatchers.withKey("change_app_theme")).perform(click());
        onData(PreferenceMatchers.withKey("change_app_language")).perform(click());
        onData(PreferenceMatchers.withKey("change_font_size")).perform(click());
        onData(PreferenceMatchers.withKey("change_navigation")).perform(click());
        onData(PreferenceMatchers.withKey("show_splash_screen")).perform(click());
        onData(PreferenceMatchers.withKey("maps")).perform(click());
        onData(PreferenceMatchers.withKey("high_resolution")).perform(ViewActions.scrollTo());
        onData(PreferenceMatchers.withKey("periodic_form_updates_check")).perform(click());
        onData(PreferenceMatchers.withKey("automatic_update")).perform(click());
        onData(PreferenceMatchers.withKey("hide_old_form_versions")).perform(click());
        onData(PreferenceMatchers.withKey("change_autosend")).perform(click());
        onData(PreferenceMatchers.withKey("delete_after_send")).perform(click());
        onData(PreferenceMatchers.withKey("default_to_finalized")).perform(click());
        onData(PreferenceMatchers.withKey("change_constraint_behavior")).perform(click());
        onData(PreferenceMatchers.withKey("analytics")).perform(ViewActions.scrollTo());
        onData(PreferenceMatchers.withKey("high_resolution")).perform(click());
        onData(PreferenceMatchers.withKey("image_size")).perform(click());
        onData(PreferenceMatchers.withKey("guidance_hint")).perform(click());
        onData(PreferenceMatchers.withKey("instance_form_sync")).perform(click());
        onData(PreferenceMatchers.withKey("change_form_metadata")).perform(click());
        onData(PreferenceMatchers.withKey("analytics")).perform(click());
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
        onData(PreferenceMatchers.withKey("reset_settings")).perform(click());
        return this;
    }

    public AdminSettingsPage uncheckServerOption() {
        clickOnString(R.string.server);
        return this;
    }

}
