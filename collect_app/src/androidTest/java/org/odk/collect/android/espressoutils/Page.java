package org.odk.collect.android.espressoutils;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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
        FormEntry.checkIsTextDisplayed(text);
        return (T) this;
    }

    public T closeSoftKeyboard() {
        Espresso.closeSoftKeyboard();
        return (T) this;
    }

    public T checkIfTextDoesNotExist(String text) {
        FormEntry.checkIfTextDoesNotExist(text);
        return (T) this;
    }

    public T checkIsStringDisplayed(int stringID) {
        onView(withText(getString(stringID))).check(matches(isDisplayed()));
        return (T) this;
    }

    public T checkIsToastWithMessageDisplayed(String message) {
        FormEntry.checkIsToastWithMessageDisplayes(message, rule.getActivity());
        return (T) this;
    }

    public T clickOnString(int stringID) {
        onView(withText(getString(stringID))).perform(click());
        return (T) this;
    }

    String getString(Integer id) {
        return rule.getActivity().getString(id);
    }
}
