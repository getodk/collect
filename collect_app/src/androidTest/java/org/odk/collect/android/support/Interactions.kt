package org.odk.collect.android.support

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import org.hamcrest.Matcher
import org.odk.collect.android.support.WaitFor.tryAgainOnFail

object Interactions {

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

    fun clickOn(view: Matcher<View>, root: Matcher<Root>? = null, assertion: () -> Unit) {
        tryAgainOnFail {
            clickOn(view, root)
            assertion()
        }
    }
}
