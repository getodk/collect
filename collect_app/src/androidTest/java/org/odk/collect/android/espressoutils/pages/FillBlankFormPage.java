package org.odk.collect.android.espressoutils.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.odk.collect.android.test.CustomMatchers.withIndex;

public class FillBlankFormPage extends Page<FillBlankFormPage> {

    public FillBlankFormPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public FillBlankFormPage assertOnPage() {
        checkIsStringDisplayed(R.string.enter_data);
        return this;
    }

    public FillBlankFormPage clickOnSortByButton() {
        onView(withId(R.id.menu_sort)).perform(click());
        return this;
    }

    public FillBlankFormPage clickMenuFilter() {
        onView(withId(R.id.menu_filter)).perform(click());
        return this;
    }

    public BlankFormSearchPage searchInBar(String query) {
        onView(withId(R.id.search_src_text)).perform(replaceText(query));
        return new BlankFormSearchPage(rule).assertOnPage();
    }

    public FillBlankFormPage checkIsFormSubtextDisplayed() {
        onView(withIndex(withId(R.id.form_subtitle2), 0)).check(matches(isDisplayed()));
        return this;
    }
}
