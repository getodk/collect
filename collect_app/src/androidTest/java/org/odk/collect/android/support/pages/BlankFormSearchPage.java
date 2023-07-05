package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;

public class BlankFormSearchPage extends Page<BlankFormSearchPage> {

    @Override
    public BlankFormSearchPage assertOnPage() {
        onView(withHint(getTranslatedString(org.odk.collect.strings.R.string.search))).check(matches(isDisplayed()));
        return this;
    }
}
