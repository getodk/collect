package org.odk.collect.fragmentstest

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import org.junit.rules.ExternalResource

/**
 * Alternative to [FragmentScenario] that allows tests to do work before launching the [Fragment]
 * (like switch out dependencies, construct intents etc) and also allows creation of multiple
 * scenarios in a test.
 */
class FragmentScenarioLauncherRule(@StyleRes private val defaultThemeResId: Int? = null) : ExternalResource() {

    private val scenarios = mutableListOf<FragmentScenario<*>>()

    fun <F : Fragment> launch(fragmentClass: Class<F>): FragmentScenario<F> {
        return FragmentScenario.launch(fragmentClass).also {
            scenarios.add(it)
        }
    }

    @JvmOverloads
    fun <F : Fragment> launchInContainer(fragmentClass: Class<F>, @StyleRes themResId: Int? = defaultThemeResId): FragmentScenario<F> {
        val scenario = if (themResId != null) {
            FragmentScenario.launchInContainer(fragmentClass, themeResId = themResId)
        } else {
            FragmentScenario.launchInContainer(fragmentClass)
        }

        return scenario.also {
            scenarios.add(it)
        }
    }

    @JvmOverloads
    fun <F : Fragment> launchDialogFragment(
        fragmentClass: Class<F>,
        fragmentArgs: Bundle? = null
    ): FragmentScenario<F> {
        return DialogFragmentTest.launchDialogFragment(fragmentClass, fragmentArgs).also {
            scenarios.add(it)
        }
    }

    override fun after() {
        scenarios.forEach(FragmentScenario<*>::close)
    }
}
