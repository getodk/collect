package org.odk.collect.android.support.pages;

import androidx.recyclerview.widget.RecyclerView;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

abstract class PreferencePage<T extends Page<T>> extends Page<T> {

    public T assertPreference(int name, String summary) {
        onView(isAssignableFrom(RecyclerView.class))
                .perform(scrollTo(allOf(hasDescendant(withText(getTranslatedString(name))), hasDescendant(withText(summary)))))
                .check(matches(isDisplayed()));

        return (T) this;
    }
}
