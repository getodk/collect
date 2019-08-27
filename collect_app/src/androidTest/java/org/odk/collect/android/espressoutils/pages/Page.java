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
 */

abstract class Page<T extends Page<T>> {

    final ActivityTestRule rule;

    Page(ActivityTestRule rule) {
        this.rule = rule;
    }

    public <D extends Page> D pressBack(Class<D> destination) {
        Espresso.pressBack();

        try {
            return destination.getDeclaredConstructor(ActivityTestRule.class).newInstance(rule);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        checkIsTextDisplayed(getString(stringID));
        return (T) this;
    }

    public T checkIsToastWithMessageDisplayed(String message) {
        onView(withText(message))
                .inRoot(withDecorView(not(is(rule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        return (T) this;
    }

    public T clickOnString(int stringID) {
        clickOnText(getString(stringID));
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

    String getString(Integer id) {
        return rule.getActivity().getString(id);
    }
}
