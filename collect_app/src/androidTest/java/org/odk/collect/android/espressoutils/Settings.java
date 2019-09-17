package org.odk.collect.android.espressoutils;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.PreferenceMatchers;

import org.odk.collect.android.espressoutils.pages.GeneralSettingsPage;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
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
}
