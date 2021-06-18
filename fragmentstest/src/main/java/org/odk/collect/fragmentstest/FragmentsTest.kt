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
 * Helpers for writing tests for Fragment that (hopefully) make interacting with [FragmentScenario]
 * less troublesome.
 */
object FragmentsTest {

    /**
     * Creates a FragmentScenario using a test theme that supports AppCompat and MaterialComponents
     * attributes but also won't explode (like MaterialComponents themes do).
     *
     * This uses [FragmentScenario.launch] here instead of [FragmentScenario.launchInContainer] as
     * per docs at https://developer.android.com/guide/fragments/test#dialog.
     */
    @JvmOverloads
    @JvmStatic
    fun <F : Fragment?> launchDialogFragment(
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
     * Slightly easier way to perform actions/assertions against a [DialogFragment]'s UI. Using
     * [onView] normally will match on a root view outside of where the dialog is displayed.
     */
    @JvmStatic
    fun onViewInDialog(viewMatcher: Matcher<View>): ViewInteraction {
        return onView(viewMatcher).inRoot(isDialog())
    }
}
