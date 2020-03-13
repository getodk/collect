package org.odk.collect.android.support.pages;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class FormMetadataPage extends Page<FormMetadataPage> {

    public FormMetadataPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public FormMetadataPage assertOnPage() {
        checkIsStringDisplayed(R.string.form_metadata_title);
        return this;
    }

    public FormMetadataPage clickEmail() {
        onView(withText(getTranslatedString(R.string.email))).perform(click());
        return this;
    }

    public FormMetadataPage clickUsername() {
        onView(withText(getTranslatedString(R.string.username))).perform(click());
        return this;
    }

    public FormMetadataPage clickPhoneNumber() {
        onView(withText(getTranslatedString(R.string.phone_number))).perform(click());
        return this;
    }

    public FormMetadataPage assertPreference(int name, String summary) {
        onView(isAssignableFrom(RecyclerView.class))
                .perform(scrollTo(allOf(hasDescendant(withText(getTranslatedString(name))), hasDescendant(withText(summary)))))
                .check(matches(isDisplayed()));

        return this;
    }
}
