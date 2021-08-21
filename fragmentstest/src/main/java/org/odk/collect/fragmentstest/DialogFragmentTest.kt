package org.odk.collect.fragmentstest

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
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
     * Creates a FragmentScenario using a test theme that supports AppCompat and MaterialComponents
     * attributes but also won't explode (like MaterialComponents themes do).
     *
     * This uses [FragmentScenario.launch] here instead of [FragmentScenario.launchInContainer] as
     * per docs at https://developer.android.com/guide/fragments/test#dialog.
     */
    @JvmOverloads
    @JvmStatic
    fun <F : Fragment> launchDialogFragment(
        fragmentClass: Class<F>,
        fragmentArgs: Bundle? = null
    ): FragmentScenario<F> {
        return FragmentScenario.launch(
            fragmentClass,
            fragmentArgs,
            R.style.Theme_DialogFragmentTest,
            null
        )
    }

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
