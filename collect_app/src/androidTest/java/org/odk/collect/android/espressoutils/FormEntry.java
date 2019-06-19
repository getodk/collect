package org.odk.collect.android.espressoutils;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

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

public final class FormEntry {

    private FormEntry() {
    }

    public static void clickOnText(String text) {
        onView(withText(text)).perform(click());
    }

    public static void checkIsStringDisplayed(int value) {
        onView(withText(getInstrumentation().getTargetContext().getString(value))).check(matches(isDisplayed()));
    }

    public static void checkIsIdDisplayed(int value) {
        onView(withId(value)).check(matches(isDisplayed()));
    }

    public static void clickOnId(int text) {
        onView(withId(text)).perform(click());
    }

    public static void clickOnString(int value) {
        onView(withText(getInstrumentation().getTargetContext().getString(value))).perform(click());
    }

    public static void checkIsTextDisplayed(String text) {
        onView(withText(text)).check(matches(isDisplayed()));
    }

    public static void putTextOnIndex(int index, String text) {
        onView(withIndex(withClassName(endsWith("Text")), index)).perform(replaceText(text));
    }

    public static void clickJumpEndButton() {
        onView(withId(R.id.jumpEndButton)).perform(click());
    }

    public static void putText(String text) {
        onView(withClassName(endsWith("EditText"))).perform(replaceText(text));
    }

    public static void checkIsToastWithMessageDisplayes(String message, ActivityTestRule main) {
        onView(withText(message)).inRoot(withDecorView(not(is(main.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    public static void clickGoToIconInForm() {
        onView(withId(R.id.menu_goto)).perform(click());
    }

    public static void checkIsToastWithStringDisplayes(int value, ActivityTestRule main) {
        onView(withText(getInstrumentation().getTargetContext().getString(value))).inRoot(withDecorView(not(is(main.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    public static void clickSaveAndExit() {
        onView(withId(R.id.save_exit_button)).perform(click());
    }

    public static void clickSignatureButton() {
        onView(withId(R.id.simple_button)).perform(click());
    }

    public static void checkIsDisplayedInTextClassAndSwipe(String message) {
        onView(withClassName(endsWith("EditText"))).check(matches(withText(message))).perform(swipeLeft());
    }

    public static void swipeToNextQuestion() {
        onView(withId(R.id.questionholder)).perform(swipeLeft());
    }
}
