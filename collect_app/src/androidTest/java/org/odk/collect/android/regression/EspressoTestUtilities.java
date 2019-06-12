package org.odk.collect.android.regression;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.PreferenceMatchers;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
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
import static org.odk.collect.android.test.CustomMatchers.withIndex;

public final class EspressoTestUtilities {

    private EspressoTestUtilities() {
    }

    public static void checkIsToastWithMessageDisplayes(String message, ActivityTestRule main) {
        onView(withText(message)).inRoot(withDecorView(not(is(main.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    public static void checkIsToastWithStringDisplayes(int value, ActivityTestRule main) {
        onView(withText(getInstrumentation().getTargetContext().getString(value))).inRoot(withDecorView(not(is(main.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    public static void clickOnMenu() {
        Espresso.openContextualActionModeOverflowMenu();
    }

    public static void clickGoToIconInForm() {
        onView(withId(R.id.menu_goto)).perform(click());
    }

    public static void startBlankForm(String text) {
        onView(withId(R.id.enter_data)).perform(click());
        onView(withText(text)).perform(click());
    }

    public static void clickFillBlankForm() {
        onView(withId(R.id.enter_data)).perform(click());
    }

    public static void clickGeneralSettings() {
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.general_preferences))).perform(click());
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

    public static void clickOnArea(String text, Integer index) {
        onView(withIndex(withClassName(endsWith(text)), index)).perform(click());
    }

    public static void clickSaveAndExit() {
        onView(withId(R.id.save_exit_button)).perform(click());
    }

    public static void clickJumpEndButton() {
        onView(withId(R.id.jumpEndButton)).perform(click());
    }

    public static void checkIsTextDisplayed(String text) {
        onView(withText(text)).check(matches(isDisplayed()));
    }

    public static void checkIsStringDisplayed(int value) {
        onView(withText(getInstrumentation().getTargetContext().getString(value))).check(matches(isDisplayed()));
    }

    public static void checkIsIdDisplayed(int value) {
        onView(withId(value)).check(matches(isDisplayed()));
    }

    public static void clickOnText(String text) {
        onView(withText(text)).perform(click());
    }

    public static void clickOnId(int text) {
        onView(withId(text)).perform(click());
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

    public static void clickSignatureButton() {
        onView(withId(R.id.simple_button)).perform(click());
    }

    public static void clickOnString(int value) {
        onView(withText(getInstrumentation().getTargetContext().getString(value))).perform(click());
    }

    public static void putText(String text) {
        onView(withClassName(endsWith("EditText"))).perform(replaceText(text));
    }

    public static void checkIsDisplayedInTextClassAndSwipe(String message) {
        onView(withClassName(endsWith("EditText"))).check(matches(withText(message))).perform(swipeLeft());
    }

    public static void putTextOnIndex(int index, String text) {
        onView(withIndex(withClassName(endsWith("Text")), index)).perform(replaceText(text));
    }

    public static void resetSettings() {
        clickOnMenu();
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.admin_preferences))).perform(click());
        onData(PreferenceMatchers.withKey("reset_settings")).perform(click());
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.reset_settings))).perform(click());
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.reset_settings_button_reset))).perform(click());
        onView(withText(getInstrumentation().getTargetContext().getString(R.string.ok))).perform(click());
    }

    public static void clickOnAreaWithIndex(String text, Integer index) {
        onView(withIndex(withClassName(endsWith(text)), index)).perform(click());
    }

    public static void clickOnAreaWithKey(String text) {
        onData(PreferenceMatchers.withKey(text)).perform(click());
    }

    public static void swipeToNextQuestion() {
        onView(withId(R.id.questionholder)).perform(swipeLeft());
    }

}