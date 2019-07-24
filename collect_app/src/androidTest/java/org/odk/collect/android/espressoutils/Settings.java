package org.odk.collect.android.espressoutils;

import androidx.test.espresso.matcher.PreferenceMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
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

    public static void resetSettings() {
        MainMenu.clickOnMenu();
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.admin_preferences))).perform(click());
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

}
