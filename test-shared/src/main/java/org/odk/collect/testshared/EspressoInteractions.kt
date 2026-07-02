package org.odk.collect.testshared

import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.odk.collect.testshared.EspressoInteractions.clickOn
import org.odk.collect.testshared.WaitFor.tryAgainOnFail

object EspressoInteractions {

    /**
     * Click on the view matched by [view]. The root to use can optionally be specified with
     * [root] (otherwise Espresso will use heuristics to determine the most likely root). If
     * initially clicking on the view fails, this will then attempt to scroll to the view and
     * retry the click.
     */
    @JvmStatic
    @JvmOverloads
    fun clickOn(view: Matcher<View>, root: Matcher<Root>? = null) {
        val onView = if (root != null) {
            onView(view).inRoot(root)
        } else {
            onView(view)
        }

        /**
         * Click on item and throw exception if Espresso detects a long press.
         */
        val clickAction = click(ExceptionRollbackAction())

        try {
            onView.perform(clickAction)
        } catch (e: Exception) {
            onView.perform(scrollTo(), clickAction)
        }
    }

    /**
     * Like [clickOn], but an [assertion] can be made after the click. If this fails, the click
     * action will be reattempted.
     *
     * This can be useful in cases where [clickOn] itself appears to succeed, but the test fails
     * because the click never actually occurs (most likely due to some flakiness in
     * [click]).
     */
    fun clickOn(view: Matcher<View>, root: Matcher<Root>? = null, assertion: () -> Unit) {
        clickOn(view, root)
        WaitFor.waitFor {
            assertion()
        }
    }

    /**
     * Replaces text in the view matched by [view] and then closes the keyboard.
     */
    @JvmStatic
    fun replaceText(view: Matcher<View>, text: String) {
        onView(view).perform(ViewActions.replaceText(text))
        closeSoftKeyboard()
    }

    private class ExceptionRollbackAction : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return object : TypeSafeMatcher<View>() {
                override fun matchesSafely(view: View): Boolean {
                    return true
                }

                override fun describeTo(description: Description) {

                }
            }
        }

        override fun getDescription(): String? {
            return null
        }

        override fun perform(
            uiController: UiController?,
            view: View?
        ) {
            throw LongPressInsteadOfClickException()
        }
    }

    class LongPressInsteadOfClickException : Exception()
}
