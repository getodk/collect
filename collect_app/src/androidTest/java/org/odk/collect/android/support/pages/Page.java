package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.support.CustomMatchers.withIndex;
import static org.odk.collect.testshared.NestedScrollToAction.nestedScrollTo;
import static org.odk.collect.android.support.matchers.RecyclerViewMatcher.withRecyclerView;

import android.app.Activity;
import android.content.pm.ActivityInfo;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.odk.collect.android.R;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.support.AdbFormLoadingUtils;
import org.odk.collect.android.support.WaitFor;
import org.odk.collect.android.support.actions.RotateAction;
import org.odk.collect.android.support.matchers.RecyclerViewMatcher;
import org.odk.collect.android.utilities.TranslationHandler;
import org.odk.collect.androidshared.ui.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import timber.log.Timber;

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

public abstract class Page<T extends Page<T>> {

    public abstract T assertOnPage();

    public <P extends Page<P>> P assertOnPage(P page) {
        return page.assertOnPage();
    }

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

    public T assertText(String... text) {
        closeSoftKeyboard();
        for (String t : text) {
            onView(allOf(withText(t), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(matches(isDisplayed()));
        }
        return (T) this;
    }

    public T assertText(int stringID) {
        assertText(getTranslatedString(stringID));
        return (T) this;
    }

    public T assertText(int stringID, Object... formatArgs) {
        assertText(getTranslatedString(stringID, formatArgs));
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

    public T assertTextDoesNotExist(String text) {
        onView(withText(text)).check(doesNotExist());
        return (T) this;
    }

    public T assertTextDoesNotExist(int string) {
        return assertTextDoesNotExist(getTranslatedString(string));
    }

    public T assertTextDoesNotExist(String... text) {
        for (String t : text) {
            onView(allOf(withText(t), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(doesNotExist());
        }
        return (T) this;
    }

    public T checkIsToastWithMessageDisplayed(String message) {
        Espresso.onIdle();
        if (!ToastUtils.popRecordedToasts().stream().anyMatch(s -> s.equals(message))) {
            throw new RuntimeException("No Toast with text \"" + message + "\" shown on screen!");
        }

        return (T) this;
    }

    public T checkIsToastWithMessageDisplayed(int stringID) {
        return checkIsToastWithMessageDisplayed(getTranslatedString(stringID));
    }

    public T checkIsToastWithMessageDisplayed(Integer id, Object... formatArgs) {
        return checkIsToastWithMessageDisplayed(getTranslatedString(id, formatArgs));
    }

    public T clickOnString(int stringID) {
        clickOnText(getTranslatedString(stringID));
        return (T) this;
    }

    public T clickOnText(String text) {
        try {
            onView(withText(text)).perform(click());
        } catch (PerformException e) {
            Timber.e(e);

            // Create a standard view match error so the view hierarchy is visible in the failure
            onView(withText("PerformException thrown clicking on \"" + text + "\"")).check(matches(isDisplayed()));
        }

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

    public <D extends Page<D>> D clickOKOnDialog(D destination) {
        closeSoftKeyboard(); // Make sure to avoid issues with keyboard being up
        clickOnId(android.R.id.button1);
        return destination.assertOnPage();
    }

    public <D extends Page<D>> D clickOnButtonInDialog(int buttonText, D destination) {
        WaitFor.wait250ms(); // https://github.com/android/android-test/issues/444
        onView(withText(getTranslatedString(buttonText)))
                .inRoot(isDialog())
                .perform(click());

        return destination.assertOnPage();
    }

    String getTranslatedString(Integer id) {
        return TranslationHandler.getString(ApplicationProvider.getApplicationContext(), id);
    }

    String getTranslatedString(Integer id, Object... formatArgs) {
        return TranslationHandler.getString(ApplicationProvider.getApplicationContext(), id, formatArgs);
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
        closeSoftKeyboard();
        return (T) this;
    }

    public T inputText(int hint, String text) {
        onView(withHint(getTranslatedString(hint))).perform(replaceText(text));
        closeSoftKeyboard();
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

    public T assertEnabled(int string) {
        onView(withText(string)).check(matches(allOf(isDisplayed(), isEnabled())));
        return (T) this;
    }

    public T assertDisabled(int string) {
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

    public static Activity getCurrentActivity() {
        getInstrumentation().waitForIdleSync();

        final Activity[] activity = new Activity[1];
        getInstrumentation().runOnMainSync(() -> {
            java.util.Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            if (!activities.isEmpty()) {
                activity[0] = Iterables.getOnlyElement(activities);
            } else {
                activity[0] = null;
            }
        });

        return activity[0];
    }

    public T checkIsSnackbarErrorVisible() {
        onView(allOf(withId(R.id.snackbar_text))).check(matches(isDisplayed()));
        return (T) this;
    }

    public T scrollToAndClickText(int text) {
        onView(withText(getTranslatedString(text))).perform(nestedScrollTo(), click());
        return (T) this;
    }

    public T scrollToAndClickText(String text) {
        onView(withText(text)).perform(nestedScrollTo(), click());
        return (T) this;
    }

    public T scrollToRecyclerViewItemAndClickText(String text) {
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions
                .actionOnItem(hasDescendant(withText(text)), scrollTo()));
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions
                .actionOnItem(hasDescendant(withText(text)), click()));
        return (T) this;
    }

    public T scrollToRecyclerViewItemAndClickText(int string) {
        onView(isAssignableFrom(RecyclerView.class)).perform(RecyclerViewActions
                .actionOnItem(hasDescendant(withText(getTranslatedString(string))), scrollTo()));
        onView(isAssignableFrom(RecyclerView.class)).perform(RecyclerViewActions
                .actionOnItem(hasDescendant(withText(getTranslatedString(string))), click()));
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

    void tryAgainOnFail(Runnable action) {
        tryAgainOnFail(action, 2);
    }

    void tryAgainOnFail(Runnable action, int maxTimes) {
        Exception failure = null;

        for (int i = 0; i < maxTimes; i++) {
            try {
                action.run();
                return;
            } catch (Exception e) {
                failure = e;
                WaitFor.wait250ms();
            }
        }

        throw new RuntimeException("tryAgainOnFail failed", failure);
    }

    protected void waitForText(String text) {
        WaitFor.waitFor(() -> assertText(text));
    }

    public T assertTextNotDisplayed(int string) {
        onView(withText(getTranslatedString(string))).check(matches(not(isDisplayed())));
        return (T) this;
    }

    protected void assertToolbarTitle(String title) {
        onView(allOf(withText(title), isDescendantOfA(withId(R.id.toolbar)))).check(matches(isDisplayed()));
    }

    protected void assertToolbarTitle(int title) {
        assertToolbarTitle(getTranslatedString(title));
    }

    public T copyForm(String formFilename) {
        try {
            AdbFormLoadingUtils.copyFormToDemoProject(formFilename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return (T) this;
    }

    public T copyForm(String formFilename, List<String> mediaFileNames) {
        try {
            AdbFormLoadingUtils.copyFormToStorage(formFilename, mediaFileNames, false, formFilename, "Demo project");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return (T) this;
    }

    public T copyForm(String formFilename, String projectName) {
        try {
            AdbFormLoadingUtils.copyFormToStorage(formFilename, projectName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return (T) this;
    }

    public T copyForm(String formFilename, List<String> mediaFileNames, String projectName) {
        try {
            AdbFormLoadingUtils.copyFormToStorage(formFilename, mediaFileNames, false, formFilename, projectName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return (T) this;
    }

    public T copyInstance(String instanceFileName) {
        try {
            AdbFormLoadingUtils.copyInstanceToDemoProject(instanceFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return (T) this;
    }

    public T assertContentDescriptionDisplayed(int string) {
        onView(withContentDescription(string)).check(matches(isDisplayed()));
        return (T) this;
    }

    public T assertContentDescriptionNotDisplayed(int string) {
        onView(withContentDescription(string)).check(matches(not(isDisplayed())));
        return (T) this;
    }

    public T clickOnContentDescription(int string) {
        onView(withContentDescription(string)).perform(click());
        return (T) this;
    }

    public T assertFileWithProjectNameUpdated(String sanitizedOldProjectName, String sanitizedNewProjectName) {
        StoragePathProvider storagePathProvider = new StoragePathProvider();
        assertFalse(new File(storagePathProvider.getProjectRootDirPath() + File.separator + sanitizedOldProjectName).exists());
        assertTrue(new File(storagePathProvider.getProjectRootDirPath() + File.separator + sanitizedNewProjectName).exists());
        return (T) this;
    }
}


