package org.odk.collect.fragmentstest

import android.view.View
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.RootMatchers.isDialog
import org.hamcrest.Matcher

/**
 * Helpers for writing tests for [androidx.fragment.app.DialogFragment] that (hopefully) make
 * interacting with [FragmentScenario] less troublesome.
 */
object DialogFragmentTest {

    /**
     * When testing a [androidx.fragment.app.DialogFragment] we need to specify the root when
     * performing view assertions/actions. Using [onView] will fail as it targets the blank
     * activity being used by the [FragmentScenario].
     */
    @JvmStatic
    fun onViewInDialog(viewMatcher: Matcher<View>): ViewInteraction {
        return onView(viewMatcher).inRoot(isDialog())
    }
}
