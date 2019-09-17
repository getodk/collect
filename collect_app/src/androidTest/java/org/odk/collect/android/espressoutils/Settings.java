package org.odk.collect.android.espressoutils;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.PreferenceMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.pages.GeneralSettingsPage;
import org.odk.collect.android.espressoutils.pages.MainMenuPage;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

/**
 * @deprecated Prefer page objects {@link GeneralSettingsPage} over static helpers
 */
@Deprecated
public final class Settings {

    private Settings() {
    }

    public static void resetSettings(ActivityTestRule rule) {
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickAdminSettings();

        onData(PreferenceMatchers.withKey("reset_settings")).perform(click());
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.reset_settings))).perform(click());
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.reset_settings_button_reset))).perform(click());
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.ok))).perform(click());
    }

    public static void clickOnString(int value) {
        onView(withText(getInstrumentation().getTargetContext().getString(value))).perform(click());
    }

    public static void checkIsTextDisplayed(String text) {
        onView(withText(text)).check(matches(isDisplayed()));
    }

    public static void openFormManagement() {
        onData(PreferenceMatchers.withKey("form_management")).perform(click());
    }

    public static void openShowGuidanceForQuestions() {
        onData(PreferenceMatchers.withKey("guidance_hint")).perform(ViewActions.scrollTo());
        onData(PreferenceMatchers.withKey("guidance_hint")).perform(click());
    }

    public static void openUserSettings() {
        onData(PreferenceMatchers.withKey("user_settings")).perform(click());
    }

    public static void uncheckAllUsetSettings() {
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
    }

    public static void checkIfStringDoesNotExist(int value) {
        onView(withText(getInstrumentation().getTargetContext().getString(value))).check(doesNotExist());
    }

    public static void openGeneralSettingsFromAdminSettings() {
        onData(PreferenceMatchers.withKey("odk_preferences")).perform(click());
    }

    public static void checkIfAreaWithKeyIsDisplayed(String text) {
        onData(PreferenceMatchers.withKey(text)).check(matches(isDisplayed()));
    }

    public static void uncheckUserSettings(String text) {
        onData(PreferenceMatchers.withKey(text)).perform(ViewActions.scrollTo());
        onData(PreferenceMatchers.withKey(text)).perform(click());
    }
}
