package org.odk.collect.android.support.pages

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.hamcrest.core.StringContains
import org.hamcrest.core.StringEndsWith
import org.junit.Assert
import org.odk.collect.android.R
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.AdbFormLoadingUtils
import org.odk.collect.android.support.CustomMatchers
import org.odk.collect.android.support.WaitFor.wait250ms
import org.odk.collect.android.support.WaitFor.waitFor
import org.odk.collect.android.support.actions.RotateAction
import org.odk.collect.android.support.matchers.RecyclerViewMatcher
import org.odk.collect.android.utilities.TranslationHandler
import org.odk.collect.androidshared.ui.ToastUtils.popRecordedToasts
import org.odk.collect.testshared.NestedScrollToAction
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
        Espresso.onView(Matchers.allOf(ViewMatchers.withText(text), ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        return this as T
    }

    fun assertText(vararg text: String?): T {
        closeSoftKeyboard()
        for (t in text) {
            Espresso.onView(Matchers.allOf(ViewMatchers.withText(t), ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
        return this as T
    }

    fun assertText(stringID: Int, vararg formatArgs: Any?): T {
        assertText(getTranslatedString(stringID, *formatArgs))
        return this as T
    }

    fun checkIsTranslationDisplayed(vararg text: String?): T {
        for (s in text) {
            try {
                Espresso.onView(ViewMatchers.withText(s)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
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
        Espresso.onView(ViewMatchers.withText(text)).check(ViewAssertions.doesNotExist())
        return this as T
    }

    fun assertTextDoesNotExist(string: Int): T {
        return assertTextDoesNotExist(getTranslatedString(string))
    }

    fun assertTextDoesNotExist(vararg text: String?): T {
        for (t in text) {
            Espresso.onView(Matchers.allOf(ViewMatchers.withText(t), ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(ViewAssertions.doesNotExist())
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

    fun checkIsToastWithMessageDisplayed(id: Int?, vararg formatArgs: Any?): T {
        return checkIsToastWithMessageDisplayed(getTranslatedString(id, *formatArgs))
    }

    fun clickOnString(stringID: Int): T {
        clickOnText(getTranslatedString(stringID))
        return this as T
    }

    fun clickOnText(text: String): T {
        try {
            Espresso.onView(ViewMatchers.withText(text)).perform(ViewActions.click())
        } catch (e: PerformException) {
            Timber.e(e)

            // Create a standard view match error so the view hierarchy is visible in the failure
            Espresso.onView(ViewMatchers.withText("PerformException thrown clicking on \"$text\"")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
        return this as T
    }

    fun clickOnId(id: Int): T {
        Espresso.onView(ViewMatchers.withId(id)).perform(ViewActions.click())
        return this as T
    }

    fun checkIsIdDisplayed(id: Int): T {
        Espresso.onView(ViewMatchers.withId(id)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
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
        Espresso.onView(ViewMatchers.withText(getTranslatedString(buttonText)))
            .inRoot(RootMatchers.isDialog())
            .perform(ViewActions.click())
        return destination!!.assertOnPage()
    }

    fun getTranslatedString(id: Int?, vararg formatArgs: Any?): String {
        return TranslationHandler.getString(ApplicationProvider.getApplicationContext(), id!!, *formatArgs)
    }

    fun clickOnAreaWithIndex(clazz: String?, index: Int): T {
        Espresso.onView(CustomMatchers.withIndex(ViewMatchers.withClassName(StringEndsWith.endsWith(clazz)), index)).perform(ViewActions.click())
        return this as T
    }

    fun addText(existingText: String?, text: String?): T {
        Espresso.onView(ViewMatchers.withText(existingText)).perform(ViewActions.typeText(text))
        return this as T
    }

    fun inputText(text: String?): T {
        Espresso.onView(ViewMatchers.withClassName(StringEndsWith.endsWith("EditText"))).perform(ViewActions.replaceText(text))
        closeSoftKeyboard()
        return this as T
    }

    fun inputText(hint: Int, text: String?): T {
        Espresso.onView(ViewMatchers.withHint(getTranslatedString(hint))).perform(ViewActions.replaceText(text))
        closeSoftKeyboard()
        return this as T
    }

    fun checkIfElementIsGone(id: Int): T {
        Espresso.onView(ViewMatchers.withId(id)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        return this as T
    }

    fun clearTheText(text: String?): T {
        Espresso.onView(ViewMatchers.withText(text)).perform(ViewActions.clearText())
        return this as T
    }

    fun checkIsTextDisplayedOnDialog(text: String?): T {
        Espresso.onView(ViewMatchers.withId(android.R.id.message)).check(ViewAssertions.matches(ViewMatchers.withText(StringContains.containsString(text))))
        return this as T
    }

    fun assertEnabled(string: Int): T {
        Espresso.onView(ViewMatchers.withText(string)).check(ViewAssertions.matches(Matchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.isEnabled())))
        return this as T
    }

    fun assertDisabled(string: Int): T {
        Espresso.onView(ViewMatchers.withText(string)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isEnabled())))
        return this as T
    }

    fun <D : Page<D>?> rotateToLandscape(destination: D): D {
        Espresso.onView(ViewMatchers.isRoot()).perform(rotateToLandscape())
        waitForRotationToEnd()
        return destination!!.assertOnPage()
    }

    fun <D : Page<D>?> rotateToPortrait(destination: D): D {
        Espresso.onView(ViewMatchers.isRoot()).perform(rotateToPortrait())
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
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.snackbar_text))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        return this as T
    }

    fun scrollToAndClickText(text: Int): T {
        Espresso.onView(ViewMatchers.withText(getTranslatedString(text))).perform(NestedScrollToAction.nestedScrollTo(), ViewActions.click())
        return this as T
    }

    fun scrollToAndClickText(text: String?): T {
        Espresso.onView(ViewMatchers.withText(text)).perform(NestedScrollToAction.nestedScrollTo(), ViewActions.click())
        return this as T
    }

    fun scrollToRecyclerViewItemAndClickText(text: String?): T {
        Espresso.onView(ViewMatchers.withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(ViewMatchers.withText(text)), ViewActions.scrollTo()))
        Espresso.onView(ViewMatchers.withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(ViewMatchers.withText(text)), ViewActions.click()))
        return this as T
    }

    fun scrollToRecyclerViewItemAndClickText(string: Int): T {
        Espresso.onView(ViewMatchers.isAssignableFrom(RecyclerView::class.java)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(ViewMatchers.withText(getTranslatedString(string))), ViewActions.scrollTo()))
        Espresso.onView(ViewMatchers.isAssignableFrom(RecyclerView::class.java)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(ViewMatchers.withText(getTranslatedString(string))), ViewActions.click()))
        return this as T
    }

    fun scrollToAndAssertText(text: String?): T {
        Espresso.onView(ViewMatchers.withText(text)).perform(NestedScrollToAction.nestedScrollTo())
        Espresso.onView(ViewMatchers.withText(text)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        return this as T
    }

    fun clickOnElementInHierarchy(index: Int): T {
        Espresso.onView(ViewMatchers.withId(R.id.list)).perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(index))
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.list).atPositionOnView(index, R.id.primary_text)).perform(ViewActions.click())
        return this as T
    }

    fun checkListSizeInHierarchy(index: Int): T {
        Espresso.onView(ViewMatchers.withId(R.id.list)).check(ViewAssertions.matches(RecyclerViewMatcher.withListSize(index)))
        return this as T
    }

    fun checkIfElementInHierarchyMatchesToText(text: String?, index: Int): T {
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.list).atPositionOnView(index, R.id.primary_text)).check(ViewAssertions.matches(ViewMatchers.withText(text)))
        return this as T
    }

    fun checkIfWebViewActivityIsDisplayed(): T {
        Espresso.onView(ViewMatchers.withClassName(StringEndsWith.endsWith("WebView"))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
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
        Espresso.onView(ViewMatchers.withText(getTranslatedString(string))).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
        return this as T
    }

    protected fun assertToolbarTitle(title: String?) {
        Espresso.onView(Matchers.allOf(ViewMatchers.withText(title), ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.toolbar)))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
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
        Espresso.onView(ViewMatchers.withContentDescription(string)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        return this as T
    }

    fun assertContentDescriptionNotDisplayed(string: Int): T {
        Espresso.onView(ViewMatchers.withContentDescription(string)).check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers.isDisplayed())))
        return this as T
    }

    fun clickOnContentDescription(string: Int): T {
        Espresso.onView(ViewMatchers.withContentDescription(string)).perform(ViewActions.click())
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
