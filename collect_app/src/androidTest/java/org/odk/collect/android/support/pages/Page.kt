package org.odk.collect.android.support.pages

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoActivityResumedException
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
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
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.StringContains.containsString
import org.hamcrest.core.StringEndsWith.endsWith
import org.junit.Assert
import org.junit.Assert.fail
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.ActivityHelpers.getLaunchIntent
import org.odk.collect.android.support.WaitFor.tryAgainOnFail
import org.odk.collect.android.support.WaitFor.wait250ms
import org.odk.collect.android.support.WaitFor.waitFor
import org.odk.collect.android.support.actions.RotateAction
import org.odk.collect.android.support.matchers.CustomMatchers.withIndex
import org.odk.collect.android.support.rules.RecentAppsRule
import org.odk.collect.android.utilities.ActionRegister
import org.odk.collect.androidshared.ui.ToastUtils.popRecordedToasts
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.strings.localization.getLocalizedQuantityString
import org.odk.collect.strings.localization.getLocalizedString
import org.odk.collect.testshared.EspressoHelpers
import org.odk.collect.testshared.RecyclerViewMatcher
import timber.log.Timber
import java.io.File

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

    fun pressBackKillingApp() {
        try {
            Espresso.pressBack()
            fail("App was not killed!")
        } catch (e: NoActivityResumedException) {
            // App killed as expected
        }
    }

    fun assertTexts(vararg texts: String): T {
        closeSoftKeyboard()
        for (text in texts) {
            assertText(text)
        }
        return this as T
    }

    fun assertText(stringID: Int, vararg formatArgs: Any): T {
        assertText(getTranslatedString(stringID, *formatArgs))
        return this as T
    }

    fun assertQuantityText(stringID: Int, quantity: Int, vararg formatArgs: Any): T {
        assertText(getTranslatedQuantityString(stringID, quantity, *formatArgs))
        return this as T
    }

    fun assertText(text: String): T {
        EspressoHelpers.assertText(text)
        return this as T
    }

    @JvmOverloads
    fun assertTextThatContainsExists(text: String, index: Int = 0): T {
        onView(
            allOf(
                withIndex(
                    withText(
                        containsString(
                            text
                        )
                    ),
                    index
                ),
                withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
            )
        ).check(matches(not(doesNotExist())))
        return this as T
    }

    @JvmOverloads
    fun assertTextThatContainsDoesNoExist(text: String, index: Int = 0): T {
        onView(
            withIndex(
                withText(
                    containsString(
                        text
                    )
                ),
                index
            )
        ).check(doesNotExist())
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

    fun assertTextsDoNotExist(vararg texts: String?): T {
        for (text in texts) {
            assertTextDoesNotExist(text)
        }
        return this as T
    }

    fun assertTextDoesNotExist(string: Int): T {
        return assertTextDoesNotExist(getTranslatedString(string))
    }

    fun assertTextDoesNotExist(text: String?): T {
        onView(allOf(withText(text), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(doesNotExist())
        return this as T
    }

    fun checkIsSnackbarWithQuantityDisplayed(message: Int, quantity: Int): T {
        return checkIsSnackbarWithMessageDisplayed(
            ApplicationProvider.getApplicationContext<Application>()
                .getLocalizedQuantityString(message, quantity, quantity)
        )
    }

    fun checkIsSnackbarWithMessageDisplayed(message: Int, vararg formatArgs: Any): T {
        return checkIsSnackbarWithMessageDisplayed(getTranslatedString(message, *formatArgs))
    }

    fun checkIsSnackbarWithMessageDisplayed(message: String): T {
        onView(withText(message)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        return this as T
    }

    fun assertToastNotDisplayed(message: String): T {
        Espresso.onIdle()
        if (popRecordedToasts().stream().anyMatch { s: String -> s == message }) {
            throw RuntimeException("Toast with text \"$message\" shown on screen!")
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
        onView(withText(text)).perform(click())
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
        waitForDialogToSettle()
        onView(withId(android.R.id.button1))
            .inRoot(isDialog())
            .perform(click())
        return this as T
    }

    fun <D : Page<D>?> clickOKOnDialog(destination: D): D {
        closeSoftKeyboard() // Make sure to avoid issues with keyboard being up
        waitForDialogToSettle()
        onView(withId(android.R.id.button1))
            .inRoot(isDialog())
            .perform(click())
        return destination!!.assertOnPage()
    }

    fun clickOnTextInDialog(text: String): T {
        waitForDialogToSettle()
        onView(withText(text))
            .inRoot(isDialog())
            .perform(click())
        return this as T
    }

    fun clickOnTextInDialog(text: Int): T {
        return clickOnTextInDialog(getTranslatedString(text))
    }

    fun <D : Page<D>> clickOnTextInDialog(text: Int, destination: D): D {
        return clickOnTextInDialog(getTranslatedString(text), destination)
    }

    fun <D : Page<D>> clickOnTextInDialog(text: String, destination: D): D {
        clickOnTextInDialog(text)
        return destination.assertOnPage()
    }

    fun getTranslatedString(id: Int?, vararg formatArgs: Any): String {
        return ApplicationProvider.getApplicationContext<Collect>().getLocalizedString(id!!, *formatArgs)
    }

    fun getTranslatedQuantityString(id: Int?, quantity: Int, vararg formatArgs: Any): String {
        return ApplicationProvider.getApplicationContext<Collect>().getLocalizedQuantityString(id!!, quantity, *formatArgs)
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
        onView(allOf(withId(com.google.android.material.R.id.snackbar_text))).check(matches(isDisplayed()))
        return this as T
    }

    fun scrollToAndClickText(text: Int): T {
        onView(withText(getTranslatedString(text))).perform(scrollTo(), click())
        return this as T
    }

    fun scrollToAndClickSubtext(text: Int): T {
        onView(withSubstring(getTranslatedString(text))).perform(scrollTo(), click())
        return this as T
    }

    fun scrollToAndClickText(text: String?): T {
        onView(withText(text)).perform(scrollTo(), click())
        return this as T
    }

    fun scrollToRecyclerViewItemAndClickText(text: String?): T {
        onView(withId(androidx.preference.R.id.recycler_view)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(text)), scrollTo()))
        onView(withId(androidx.preference.R.id.recycler_view)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(text)), click()))
        return this as T
    }

    fun scrollToRecyclerViewItemAndClickText(string: Int): T {
        onView(ViewMatchers.isAssignableFrom(RecyclerView::class.java)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(getTranslatedString(string))), scrollTo()))
        onView(ViewMatchers.isAssignableFrom(RecyclerView::class.java)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(getTranslatedString(string))), click()))
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

    private fun waitForDialogToSettle() {
        wait250ms() // https://github.com/android/android-test/issues/444
    }

    protected fun waitForText(text: String) {
        waitFor { assertText(text) }
    }

    protected fun assertToolbarTitle(title: String?) {
        onView(allOf(withText(title), isDescendantOfA(withId(org.odk.collect.androidshared.R.id.toolbar)))).check(matches(isDisplayed()))
    }

    protected fun assertToolbarTitle(title: Int) {
        assertToolbarTitle(getTranslatedString(title))
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
        EspressoHelpers.clickOnContentDescription(string)
        return this as T
    }

    fun assertFileWithProjectNameUpdated(sanitizedOldProjectName: String, sanitizedNewProjectName: String): T {
        val storagePathProvider = StoragePathProvider()
        Assert.assertFalse(File(storagePathProvider.getProjectRootDirPath() + File.separator + sanitizedOldProjectName).exists())
        Assert.assertTrue(File(storagePathProvider.getProjectRootDirPath() + File.separator + sanitizedNewProjectName).exists())
        return this as T
    }

    fun assertTextInDialog(text: String): T {
        onView(withText(text)).inRoot(isDialog()).check(matches(isDisplayed()))
        return this as T
    }

    fun assertTextInDialog(text: Int): T {
        return assertTextInDialog(getTranslatedString(text))
    }

    fun closeSnackbar(): T {
        onView(withContentDescription(org.odk.collect.strings.R.string.close_snackbar)).perform(click())
        return this as T
    }

    fun clickOptionsIcon(@StringRes expectedOptionString: Int): T {
        return clickOptionsIcon(getTranslatedString(expectedOptionString))
    }

    fun clickOptionsIcon(expectedOptionString: String): T {
        tryAgainOnFail {
            onView(OVERFLOW_BUTTON_MATCHER).perform(click())
            assertText(expectedOptionString)
        }

        return this as T
    }

    fun <D : Page<D>?> minimizeAndReopenApp(destination: D): D {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // minimize
        device.pressHome()

        // reopen
        InstrumentationRegistry.getInstrumentation().targetContext.apply {
            val intent = packageManager.getLaunchIntentForPackage(BuildConfig.APPLICATION_ID)!!
            startActivity(intent)
        }
        return destination!!.assertOnPage()
    }

    fun <D : Page<D>> killAndReopenApp(
        launcherRule: ActivityScenarioLauncherRule,
        recentAppsRule: RecentAppsRule,
        destination: D
    ): D {
        recentAppsRule.leaveAndKillApp()

        // reopen
        launcherRule.launch<Activity>(getLaunchIntent())
        return destination.assertOnPage()
    }

    fun assertNoOptionsMenu(): T {
        onView(OVERFLOW_BUTTON_MATCHER).check(doesNotExist())
        return this as T
    }

    fun longClickOnText(text: String): T {
        onView(withText(text)).perform(longClick())
        return this as T
    }

    fun clickOnTextInPopup(text: Int): T {
        onView(withText(text)).inRoot(isPlatformPopup()).perform(click())
        return this as T
    }

    fun tryFlakyAction(action: Runnable) {
        tryAgainOnFail {
            ActionRegister.attemptingAction()
            action.run()
            waitFor {
                if (!ActionRegister.isActionDetected) {
                    throw java.lang.RuntimeException("Action never detected!")
                }
            }
        }
    }

    companion object {
        private fun rotateToLandscape(): ViewAction {
            return RotateAction(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        }

        private fun rotateToPortrait(): ViewAction {
            return RotateAction(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }

        private val OVERFLOW_BUTTON_MATCHER: Matcher<View> = Matchers.anyOf(
            allOf(isDisplayed(), withContentDescription("More options")),
            allOf(isDisplayed(), withClassName(Matchers.endsWith("OverflowMenuButton")))
        )
    }
}
