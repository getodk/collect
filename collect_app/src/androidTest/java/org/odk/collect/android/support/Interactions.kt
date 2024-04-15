package org.odk.collect.android.support

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import org.hamcrest.Matcher
import org.odk.collect.android.support.WaitFor.tryAgainOnFail

object Interactions {

    /**
     * Click on the view matched by [view]. The root to use can optionally be specified with
     * [root] (otherwise Espresso will use heuristics to determine the most likely root). If
     * initially clicking on the view fails, this will then attempt to scroll to the view and
     * retry the click.
     */
    fun clickOn(view: Matcher<View>, root: Matcher<Root>? = null) {
        val onView = if (root != null) {
            onView(view).inRoot(root)
        } else {
            onView(view)
        }

        try {
            onView.perform(click())
        } catch (e: Exception) {
            onView.perform(scrollTo(), click())
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
}
