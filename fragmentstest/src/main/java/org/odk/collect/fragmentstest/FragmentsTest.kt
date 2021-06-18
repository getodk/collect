package org.odk.collect.fragmentstest

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.RootMatchers.isDialog
import org.hamcrest.Matcher

object FragmentsTest {

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

    @JvmStatic
    fun onViewInDialog(viewMatcher: Matcher<View>): ViewInteraction {
        return onView(viewMatcher).inRoot(isDialog())
    }
}
