package org.odk.collect.android.espressoutils.pages;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

/**
 * Base class for Page Objects (https://www.martinfowler.com/bliki/PageObject.html)
 * used in Espresso tests. Provides shared helpers/setup.
 *
 * This class's generic type is designed to it's extending class so that shared methods
 * can return the extending class.
 */

abstract class Page<T extends Page<T>> {

    final ActivityTestRule rule;

    Page(ActivityTestRule rule) {
        this.rule = rule;
    }

    public abstract T assertOnPage();

    /**
     * Presses back and then returns the Page object passed in after
     * asserting we're there
     */
    public <D extends Page<D>> D pressBack(D destination) {
        Espresso.pressBack();
        return destination.assertOnPage();
    }

    public T checkIsTextDisplayed(String text) {
        onView(withText(text)).check(matches(isDisplayed()));
        return (T) this;
    }

    public T closeSoftKeyboard() {
        Espresso.closeSoftKeyboard();
        return (T) this;
    }

    public T checkIfTextDoesNotExist(String text) {
        onView(withText(text)).check(doesNotExist());
        return (T) this;
    }

    public T checkIsStringDisplayed(int stringID) {
        checkIsTextDisplayed(getTranslatedString(stringID));
        return (T) this;
    }

    public T checkIsToastWithMessageDisplayed(String message) {
        onView(withText(message))
                .inRoot(withDecorView(not(is(rule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        return (T) this;
    }

    public T clickOnString(int stringID) {
        clickOnText(getTranslatedString(stringID));
        return (T) this;
    }

    public T clickOnText(String text) {
        onView(withText(text)).perform(click());
        return (T) this;
    }

    public T clickOnId(int id) {
        onView(withId(id)).perform(click());
        return (T) this;
    }

    public T checkIsIdDisplayed(int id) {
        onView(withId(id)).check(matches(isDisplayed()));
        return (T) this;
    }

    public T clickOk() {
        clickOnId(android.R.id.button1);
        return (T) this;
    }

    String getTranslatedString(Integer id) {
        return rule.getActivity().getString(id);
    }
}
