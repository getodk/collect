package org.odk.collect.fragmentstest

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario

object FragmentTest {

    @JvmOverloads
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
}
