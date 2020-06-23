package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class ExperimentalPage extends Page<ExperimentalPage> {

    ExperimentalPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public ExperimentalPage assertOnPage() {
        onView(allOf(withText(getTranslatedString(R.string.experimental)), isDescendantOfA(withId(R.id.toolbar)))).check(matches(isDisplayed()));
        return this;
    }

    public ExperimentalPage clickMatchExactly() {
        clickOnString(R.string.match_exactly);
        return this;
    }
}
