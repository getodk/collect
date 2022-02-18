package org.odk.collect.android.support.pages

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.StringContains.containsString
import org.hamcrest.core.StringEndsWith.endsWith
import org.junit.Assert
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.AdbFormLoadingUtils
import org.odk.collect.android.support.WaitFor.wait250ms
import org.odk.collect.android.support.WaitFor.waitFor
import org.odk.collect.android.support.actions.RotateAction
import org.odk.collect.android.support.matchers.CustomMatchers.withIndex
import org.odk.collect.android.support.matchers.RecyclerViewMatcher
import org.odk.collect.androidshared.ui.ToastUtils.popRecordedToasts
import org.odk.collect.androidtest.NestedScrollToAction.nestedScrollTo
import org.odk.collect.strings.localization.getLocalizedString
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Base class for Page Objects used in Espresso tests. Provides shared helpers/setup.
 *
 *
 * Sub classes of `Page` should represent a page or part of a "page" that the user would
 * interact with. Operations on these objects should return `this` (unless
 * transitioning to a new page) so that they can be used in a fluent style.
 *
 *
 * The generic typing is a little strange here but enables shared methods such as
 * [Page.closeSoftKeyboard] to return the sub class type rather than `Page` so
 * operations can be chained without casting. For example `FooPage` would extend
 * `Page<FooPage>` and calling `fooPage.closeSoftKeyboard()` would return
 * a `FooPage`.
 *
 * @see [Page Objects](https://www.martinfowler.com/bliki/PageObject.html)
 *
 * @see [Fluent Interfaces](https://en.wikipedia.org/wiki/Fluent_interface)
 */

@Suppress("UNCHECKED_CAST")
abstract class Page<T : Page<T>> {

    abstract fun assertOnPage(): T

    fun <P : Page<P>> assertOnPage(page: P): P {
        return page.assertOnPage()
    }

    /**
     * Presses back and then returns the Page object passed in after
     * asserting we're there
     */
    fun <D : Page<D>> pressBack(destination: D): D {
        Espresso.pressBack()
        return destination.assertOnPage()
    }

    fun assertText(text: String?): T {
        onView(allOf(withText(text), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(matches(isDisplayed()))
        return this as T
    }

    fun assertText(vararg text: String?): T {
        closeSoftKeyboard()
        for (t in text) {
            onView(allOf(withText(t), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(matches(isDisplayed()))
        }
        return this as T
    }

    fun assertText(stringID: Int, vararg formatArgs: Any): T {
        assertText(getTranslatedString(stringID, *formatArgs))
        return this as T
    }

    fun checkIsTranslationDisplayed(vararg text: String?): T {
        for (s in text) {
            try {
                onView(withText(s)).check(matches(isDisplayed()))
            } catch (e: NoMatchingViewException) {
                Timber.i(e)
            }
        }
        return this as T
    }

    fun closeSoftKeyboard(): T {
        Espresso.closeSoftKeyboard()
        return this as T
    }

    fun assertTextDoesNotExist(text: String?): T {
        onView(withText(text)).check(doesNotExist())
        return this as T
    }

    fun assertTextDoesNotExist(string: Int): T {
        return assertTextDoesNotExist(getTranslatedString(string))
    }

    fun assertTextDoesNotExist(vararg text: String?): T {
        for (t in text) {
            onView(allOf(withText(t), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(doesNotExist())
        }
        return this as T
    }

    fun checkIsToastWithMessageDisplayed(message: String): T {
        Espresso.onIdle()
        if (!popRecordedToasts().stream().anyMatch { s: String -> s == message }) {
            throw RuntimeException("No Toast with text \"$message\" shown on screen!")
        }
        return this as T
    }

    fun checkIsToastWithMessageDisplayed(id: Int?, vararg formatArgs: Any): T {
        return checkIsToastWithMessageDisplayed(getTranslatedString(id, *formatArgs))
    }

    fun clickOnString(stringID: Int): T {
        clickOnText(getTranslatedString(stringID))
        return this as T
    }

    fun clickOnText(text: String): T {
        try {
            onView(withText(text)).perform(click())
        } catch (e: PerformException) {
            Timber.e(e)

            // Create a standard view match error so the view hierarchy is visible in the failure
            onView(withText("PerformException thrown clicking on \"$text\"")).check(matches(isDisplayed()))
        }
        return this as T
    }

    fun clickOnId(id: Int): T {
        onView(withId(id)).perform(click())
        return this as T
    }

    fun checkIsIdDisplayed(id: Int): T {
        onView(withId(id)).check(matches(isDisplayed()))
        return this as T
    }

    fun clickOKOnDialog(): T {
        closeSoftKeyboard() // Make sure to avoid issues with keyboard being up
        clickOnId(android.R.id.button1)
        return this as T
    }

    fun <D : Page<D>?> clickOKOnDialog(destination: D): D {
        closeSoftKeyboard() // Make sure to avoid issues with keyboard being up
        clickOnId(android.R.id.button1)
        return destination!!.assertOnPage()
    }

    fun <D : Page<D>?> clickOnButtonInDialog(buttonText: Int, destination: D): D {
        wait250ms() // https://github.com/android/android-test/issues/444
        onView(withText(getTranslatedString(buttonText)))
            .inRoot(isDialog())
            .perform(click())
        return destination!!.assertOnPage()
    }

    fun getTranslatedString(id: Int?, vararg formatArgs: Any): String {
        return ApplicationProvider.getApplicationContext<Collect>().getLocalizedString(id!!, *formatArgs)
    }

    fun clickOnAreaWithIndex(clazz: String?, index: Int): T {
        onView(withIndex(withClassName(endsWith(clazz)), index)).perform(click())
        return this as T
    }

    fun addText(existingText: String?, text: String?): T {
        onView(withText(existingText)).perform(typeText(text))
        return this as T
    }

    fun inputText(text: String?): T {
        onView(withClassName(endsWith("EditText"))).perform(replaceText(text))
        closeSoftKeyboard()
        return this as T
    }

    fun inputText(hint: Int, text: String?): T {
        onView(withHint(getTranslatedString(hint))).perform(replaceText(text))
        closeSoftKeyboard()
        return this as T
    }

    fun checkIfElementIsGone(id: Int): T {
        onView(withId(id)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        return this as T
    }

    fun clearTheText(text: String?): T {
        onView(withText(text)).perform(ViewActions.clearText())
        return this as T
    }

    fun checkIsTextDisplayedOnDialog(text: String?): T {
        onView(withId(android.R.id.message)).check(matches(withText(containsString(text))))
        return this as T
    }

    fun assertEnabled(string: Int): T {
        onView(withText(string)).check(matches(allOf(isDisplayed(), isEnabled())))
        return this as T
    }

    fun assertDisabled(string: Int): T {
        onView(withText(string)).check(matches(not(isEnabled())))
        return this as T
    }

    fun <D : Page<D>?> rotateToLandscape(destination: D): D {
        onView(isRoot()).perform(rotateToLandscape())
        waitForRotationToEnd()
        return destination!!.assertOnPage()
    }

    fun <D : Page<D>?> rotateToPortrait(destination: D): D {
        onView(isRoot()).perform(rotateToPortrait())
        waitForRotationToEnd()
        return destination!!.assertOnPage()
    }

    fun waitForRotationToEnd(): T {
        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            Timber.i(e)
        }
        return this as T
    }

    fun checkIsSnackbarErrorVisible(): T {
        onView(allOf(withId(R.id.snackbar_text))).check(matches(isDisplayed()))
        return this as T
    }

    fun scrollToAndClickText(text: Int): T {
        onView(withText(getTranslatedString(text))).perform(nestedScrollTo(), click())
        return this as T
    }

    fun scrollToAndClickText(text: String?): T {
        onView(withText(text)).perform(nestedScrollTo(), click())
        return this as T
    }

    fun scrollToRecyclerViewItemAndClickText(text: String?): T {
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(text)), scrollTo()))
        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(text)), click()))
        return this as T
    }

    fun scrollToRecyclerViewItemAndClickText(string: Int): T {
        onView(ViewMatchers.isAssignableFrom(RecyclerView::class.java)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(getTranslatedString(string))), scrollTo()))
        onView(ViewMatchers.isAssignableFrom(RecyclerView::class.java)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(getTranslatedString(string))), click()))
        return this as T
    }

    fun scrollToAndAssertText(text: String?): T {
        onView(withText(text)).perform(nestedScrollTo())
        onView(withText(text)).check(matches(isDisplayed()))
        return this as T
    }

    fun clickOnElementInHierarchy(index: Int): T {
        onView(withId(R.id.list)).perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(index))
        onView(RecyclerViewMatcher.withRecyclerView(R.id.list).atPositionOnView(index, R.id.primary_text)).perform(click())
        return this as T
    }

    fun checkListSizeInHierarchy(index: Int): T {
        onView(withId(R.id.list)).check(matches(RecyclerViewMatcher.withListSize(index)))
        return this as T
    }

    fun checkIfElementInHierarchyMatchesToText(text: String?, index: Int): T {
        onView(RecyclerViewMatcher.withRecyclerView(R.id.list).atPositionOnView(index, R.id.primary_text)).check(matches(withText(text)))
        return this as T
    }

    fun checkIfWebViewActivityIsDisplayed(): T {
        onView(withClassName(endsWith("WebView"))).check(matches(isDisplayed()))
        return this as T
    }

    @JvmOverloads
    fun tryAgainOnFail(action: Runnable, maxTimes: Int = 2) {
        var failure: Exception? = null
        for (i in 0 until maxTimes) {
            try {
                action.run()
                return
            } catch (e: Exception) {
                failure = e
                wait250ms()
            }
        }
        throw RuntimeException("tryAgainOnFail failed", failure)
    }

    protected fun waitForText(text: String?) {
        waitFor { assertText(text) }
    }

    fun assertTextNotDisplayed(string: Int): T {
        onView(withText(getTranslatedString(string))).check(matches(not(isDisplayed())))
        return this as T
    }

    protected fun assertToolbarTitle(title: String?) {
        onView(allOf(withText(title), isDescendantOfA(withId(R.id.toolbar)))).check(matches(isDisplayed()))
    }

    protected fun assertToolbarTitle(title: Int) {
        assertToolbarTitle(getTranslatedString(title))
    }

    @JvmOverloads
    fun copyForm(formFilename: String, mediaFileNames: List<String>? = null, copyToDatabase: Boolean = false, projectName: String = "Demo project"): T {
        try {
            AdbFormLoadingUtils.copyFormToStorage(formFilename, mediaFileNames, copyToDatabase, formFilename, projectName)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return this as T
    }

    fun copyInstance(instanceFileName: String): T {
        try {
            AdbFormLoadingUtils.copyInstanceToDemoProject(instanceFileName)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return this as T
    }

    fun assertContentDescriptionDisplayed(string: Int): T {
        onView(withContentDescription(string)).check(matches(isDisplayed()))
        return this as T
    }

    fun assertContentDescriptionNotDisplayed(string: Int): T {
        onView(withContentDescription(string)).check(matches(not(isDisplayed())))
        return this as T
    }

    fun clickOnContentDescription(string: Int): T {
        onView(withContentDescription(string)).perform(click())
        return this as T
    }

    fun assertFileWithProjectNameUpdated(sanitizedOldProjectName: String, sanitizedNewProjectName: String): T {
        val storagePathProvider = StoragePathProvider()
        Assert.assertFalse(File(storagePathProvider.getProjectRootDirPath() + File.separator + sanitizedOldProjectName).exists())
        Assert.assertTrue(File(storagePathProvider.getProjectRootDirPath() + File.separator + sanitizedNewProjectName).exists())
        return this as T
    }

    companion object {
        private fun rotateToLandscape(): ViewAction {
            return RotateAction(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        }

        private fun rotateToPortrait(): ViewAction {
            return RotateAction(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }

        val currentActivity: Activity?
            get() {
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                val activity = arrayOfNulls<Activity>(1)
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    val activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                    if (!activities.isEmpty()) {
                        activity[0] = Iterables.getOnlyElement(activities)
                    } else {
                        activity[0] = null
                    }
                }
                return activity[0]
            }
    }
}
