package org.odk.collect.android.support.pages;

import android.app.Activity;
import android.content.pm.ActivityInfo;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAction;

import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.odk.collect.android.R;
import org.odk.collect.android.support.actions.RotateAction;
import org.odk.collect.android.support.matchers.RecyclerViewMatcher;

import java.util.List;

import timber.log.Timber;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.odk.collect.android.support.actions.NestedScrollToAction.nestedScrollTo;
import static org.odk.collect.android.support.matchers.RecyclerViewMatcher.withRecyclerView;
import static org.odk.collect.android.test.CustomMatchers.withIndex;

/**
 * Base class for Page Objects used in Espresso tests. Provides shared helpers/setup.
 * <p>
 * Sub classes of {@code Page} should represent a page or part of a "page" that the user would
 * interact with. Operations on these objects should return {@code this} (unless
 * transitioning to a new page) so that they can be used in a fluent style.
 * <p>
 * The generic typing is a little strange here but enables shared methods such as
 * {@link Page#closeSoftKeyboard()} to return the sub class type rather than {@code Page} so
 * operations can be chained without casting. For example {@code FooPage} would extend
 * {@code Page<FooPage>} and calling {@code fooPage.closeSoftKeyboard()} would return
 * a {@code FooPage}.
 *
 * @see <a href="https://www.martinfowler.com/bliki/PageObject.html">Page Objects</a>
 * @see <a href="https://en.wikipedia.org/wiki/Fluent_interface">Fluent Interfaces</a>
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

    public T assertText(String text) {
        onView(allOf(withText(text), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(matches(isDisplayed()));
        return (T) this;
    }

    public T assertText(String...  text) {
        for (String t : text) {
            onView(allOf(withText(t), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(matches(isDisplayed()));
        }
        return (T) this;
    }

    public T checkIsTranslationDisplayed(String... text) {
        for (String s : text) {
            try {
                onView(withText(s)).check(matches(isDisplayed()));
            } catch (NoMatchingViewException e) {
                Timber.i(e);
            }
        }
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

    public T checkIfTextDoesNotExist(int string) {
        return checkIfTextDoesNotExist(getTranslatedString(string));
    }

    public T checkIsStringDisplayed(int stringID) {
        assertText(getTranslatedString(stringID));
        return (T) this;
    }

    public T checkIsToastWithMessageDisplayed(String message) {
        onView(withText(message))
                .inRoot(withDecorView(not(is(getCurrentActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        return (T) this;
    }

    public T checkIsToastWithMessageDisplayed(int stringID) {
        return checkIsToastWithMessageDisplayed(getTranslatedString(stringID));
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

    public T clickOKOnDialog() {
        closeSoftKeyboard(); // Make sure to avoid issues with keyboard being up
        clickOnId(android.R.id.button1);
        return (T) this;
    }

    String getTranslatedString(Integer id) {
        return getCurrentActivity().getString(id);
    }

    String getTranslatedString(Integer id, List... args) {
        return getCurrentActivity().getString(id, args);
    }

    String getTranslatedString(Integer id, Object... formatArgs) {
        return getCurrentActivity().getString(id, formatArgs);
    }

    public T clickOnAreaWithIndex(String clazz, int index) {
        onView(withIndex(withClassName(endsWith(clazz)), index)).perform(click());
        return (T) this;
    }

    public T addText(String existingText, String text) {
        onView(withText(existingText)).perform(typeText(text));
        return (T) this;
    }

    public T inputText(String text) {
        onView(withClassName(endsWith("EditText"))).perform(replaceText(text));
        return (T) this;
    }

    public T checkIfElementIsGone(int id) {
        onView(withId(id)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        return (T) this;
    }

    public T clearTheText(String text) {
        onView(withText(text)).perform(clearText());
        return (T) this;
    }

    public T checkIsTextDisplayedOnDialog(String text) {
        onView(withId(android.R.id.message)).check(matches(withText(containsString(text))));
        return (T) this;
    }

    public T checkIfOptionIsDisabled(int string) {
        onView(withText(string)).check(matches(not(isEnabled())));
        return (T) this;
    }

    public <D extends Page<D>> D rotateToLandscape(D destination) {
        onView(isRoot()).perform(rotateToLandscape());
        waitForRotationToEnd();

        return destination.assertOnPage();
    }

    private static ViewAction rotateToLandscape() {
        return new RotateAction(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public <D extends Page<D>> D rotateToPortrait(D destination) {
        onView(isRoot()).perform(rotateToPortrait());
        waitForRotationToEnd();

        return destination.assertOnPage();
    }

    private static ViewAction rotateToPortrait() {
        return new RotateAction(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public T waitForRotationToEnd() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Timber.i(e);
        }

        return (T) this;
    }

    private static Activity getCurrentActivity() {
        getInstrumentation().waitForIdleSync();

        final Activity[] activity = new Activity[1];
        getInstrumentation().runOnMainSync(() -> {
            java.util.Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            activity[0] = Iterables.getOnlyElement(activities);
        });

        return activity[0];
    }

    public T checkIsSnackbarErrorVisible() {
        onView(allOf(withId(R.id.snackbar_text))).check(matches(isDisplayed()));
        return (T) this;
    }

    public T scrollToAndClickText(String text) {
        onView(withText(text)).perform(nestedScrollTo(), click());
        return (T) this;
    }

    public T scrollToAndAssertText(String text) {
        onView(withText(text)).perform(nestedScrollTo());
        onView(withText(text)).check(matches(isDisplayed()));
        return (T) this;
    }

    public T clickOnElementInHierarchy(int index) {
        onView(withId(R.id.list)).perform(scrollToPosition(index));
        onView(withRecyclerView(R.id.list).atPositionOnView(index, R.id.primary_text)).perform(click());
        return (T) this;
    }

    public T checkListSizeInHierarchy(int index) {
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(index)));
        return (T) this;
    }

    public T checkIfElementInHierarchyMatchesToText(String text, int index) {
        onView(withRecyclerView(R.id.list).atPositionOnView(index, R.id.primary_text)).check(matches(withText(text)));
        return (T) this;
    }

    public T checkIfWebViewActivityIsDisplayed() {
        onView(withClassName(endsWith("WebView"))).check(matches(isDisplayed()));
        return (T) this;
    }

    void waitForText(String text) {
        while (true) {
            try {
                assertText(text);
                break;
            } catch (NoMatchingViewException ignored) {
                // ignored
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
                // ignored
            }
        }
    }
}


