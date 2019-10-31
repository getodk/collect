package org.odk.collect.android.espressoutils;

import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.pages.FormEntryPage;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.core.StringEndsWith.endsWith;

/**
 * @deprecated Prefer page objects {@link FormEntryPage} over static helpers
 */
@Deprecated
public final class FormEntry {

    private FormEntry() {

    }

    public static void clickOnText(String text) {
        onView(withText(text)).perform(click());
    }

    public static void checkIsStringDisplayed(int value) {
        onView(withText(getInstrumentation().getTargetContext().getString(value))).check(matches(isDisplayed()));
    }

    public static void clickOnString(int value) {
        onView(withText(getInstrumentation().getTargetContext().getString(value))).perform(click());
    }

    public static void checkIsTextDisplayed(String text) {
        onView(withText(text)).check(matches(isDisplayed()));
    }

    public static void checkIfTextDoesNotExist(String text) {
        onView(withText(text)).check(doesNotExist());
    }

    public static void clickJumpStartButton() {
        onView(withId(R.id.jumpBeginningButton)).perform(click());
    }

    public static void putText(String text) {
        onView(withClassName(endsWith("EditText"))).perform(replaceText(text));
    }

    public static void clickGoToIconInForm() {
        onView(withId(R.id.menu_goto)).perform(click());
    }

    public static void clickSaveAndExit() {
        onView(withId(R.id.save_exit_button)).perform(click());
    }

    public static void swipeToNextQuestion() {
        onView(withId(R.id.questionholder)).perform(swipeLeft());
    }

    public static void deleteGroup() {
        onView(withId(R.id.menu_delete_child)).perform(click());
        onView(withText(R.string.delete_repeat)).perform(click());
    }

    public static void clickOk() {
        onView(withId(android.R.id.button1)).perform(click());
    }
}
