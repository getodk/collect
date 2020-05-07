package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class CustomServerPathsPage extends Page<CustomServerPathsPage> {

    public CustomServerPathsPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public CustomServerPathsPage assertOnPage() {
        onView(allOf(withText(getTranslatedString(R.string.custom_server_paths)), isDescendantOfA(withId(R.id.toolbar)))).check(matches(isDisplayed()));
        return this;
    }

    public CustomServerPathsPage clickFormListPath() {
        onView(withText(getTranslatedString(R.string.formlist_url))).perform(click());
        return this;
    }

    public CustomServerPathsPage clickSubmissionPath() {
        onView(withText(getTranslatedString(R.string.submission_url))).perform(click());
        return this;
    }
}
