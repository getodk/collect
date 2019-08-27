package org.odk.collect.android.espressoutils;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.PreferenceMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.pages.MainMenuPage;
import org.odk.collect.android.espressoutils.pages.GeneralSettingsPage;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.StringEndsWith.endsWith;

/**
 * @deprecated Prefer page objects {@link GeneralSettingsPage} over static helpers
 */
@Deprecated
public final class Settings {

    private Settings() {
    }

    public static void clickUserAndDeviceIdentity() {
        onData(PreferenceMatchers.withKey("user_and_device_identity")).perform(click());
    }

    public static void clickFormMetadata() {
        onData(PreferenceMatchers.withKey("form_metadata")).perform(click());
    }

    public static void clickMetadataEmail() {
        onData(PreferenceMatchers.withKey("metadata_email")).perform(click());
    }

    public static void clickMetadataUsername() {
        onData(PreferenceMatchers.withKey("metadata_username")).perform(click());
    }

    public static void openServerSettings() {
        onData(PreferenceMatchers.withKey("protocol")).perform(click());
    }

    public static void clickOnServerType() {
        onData(PreferenceMatchers.withKey("protocol")).perform(click());
    }

    public static void clickAggregateUsername() {
        onData(PreferenceMatchers.withKey("username")).perform(click());
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

    public static void checkIsToastWithStringDisplayes(int value, ActivityTestRule main) {
        onView(withText(getInstrumentation().getTargetContext().getString(value))).inRoot(withDecorView(not(is(main.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    public static void checkIsTextDisplayed(String text) {
        onView(withText(text)).check(matches(isDisplayed()));
    }

    public static final class Dialog {

        private Dialog() {
        }

        public static void putText(String text) {
            onView(withClassName(endsWith("EditText"))).perform(replaceText(text), closeSoftKeyboard());
        }

        public static void clickOK() {
            onView(withId(android.R.id.button1)).perform(click());
        }
    }

    public static void clickOnUserInterface() {
        onData(PreferenceMatchers.withKey("user_interface")).perform(click());
    }

    public static void clickOnLanguage() {
        onData(PreferenceMatchers.withKey("app_language")).perform(click());
    }

    public static void clickOnSelectedLanguage(String text) {
        onView(withText(text)).perform(click());
    }

    public static void clickUserInterface() {
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.client))).perform(click());
    }

    public static void clickNavigation() {
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.navigation))).perform(click());
    }

    public static void clickUseSwipesAndButtons() {
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.swipe_buttons_navigation))).perform(click());
    }

    public static void openFormManagement() {
        onData(PreferenceMatchers.withKey("form_management")).perform(click());
    }

    public static void openShowGuidanceForQuestions() {
        onData(PreferenceMatchers.withKey("guidance_hint")).perform(ViewActions.scrollTo());
        onData(PreferenceMatchers.withKey("guidance_hint")).perform(click());
    }

    public static void openConstraintProcessing() {
        onData(PreferenceMatchers.withKey("constraint_behavior")).perform(ViewActions.scrollTo());
        onData(PreferenceMatchers.withKey("constraint_behavior")).perform(click());
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
