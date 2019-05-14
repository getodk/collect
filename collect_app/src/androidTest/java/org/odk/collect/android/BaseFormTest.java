package org.odk.collect.android;

import android.Manifest;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.odk.collect.android.activities.MainMenuActivity;

import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;


public class BaseFormTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> main = new ActivityTestRule<>(MainMenuActivity.class);
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    protected static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                description.appendText(" , ");
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

    protected void clickGoToIcon() {
        onView(withId(R.id.menu_goto)).perform(click());
    }

    protected void clickFillBlankForm() {
        onView(withId(R.id.enter_data)).perform(click());
    }

    protected void clickOptionsIcon() {
        onView(withContentDescription("More options")).perform(click());
    }

    protected void clickGeneralSettings() {
        onView(withText("General Settings")).perform(click());
    }

    protected void clickFormManagement() {
        onView(withText("Form management")).perform(click());
    }

    protected void showGuidanceDialog() {
        onView(withText("Show guidance for questions")).perform(click());
    }

    protected void clickShowGuidanceAlways() {
        onView(withText("Yes - always shown")).perform(click());
    }

    protected void clickSaveAndExit() {
        onView(withId(R.id.save_exit_button)).perform(click());
    }

    protected void clickShowGuidanceCollapsed() {
        onView(withText("Yes - collapsed")).perform(click());
    }

    protected void clickJumpEndButton() {
        onView(withId(R.id.jumpEndButton)).perform(click());
    }

    protected void checkIsDisplayed(String message) {
        onView(withText(message)).check(matches(isDisplayed()));
    }

    protected void checkIsToastWithMessageDisplayes(String message) {
        onView(withText(message)).inRoot(withDecorView(not(is(main.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

    protected void clickOnText(String message) {
        onView(withText(message)).perform(click());
    }

}
