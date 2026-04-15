package org.odk.collect.testshared

import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import org.hamcrest.Matcher
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
         * Click on the view again in an attempt to clear any long press UI if a long press might
         * have occurred. More discussion of this issue can be found at https://stackoverflow.com/questions/32330671/android-espresso-performs-longclick-instead-of-click.
         */
        val clickAction = click(click())

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
     * [androidx.test.espresso.action.ViewActions.click]).
     */
    fun clickOn(view: Matcher<View>, root: Matcher<Root>? = null, assertion: () -> Unit) {
        tryAgainOnFail {
            clickOn(view, root)
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
}
