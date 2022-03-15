package org.odk.collect.fragmentstest

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import org.junit.rules.ExternalResource

/**
 * Alternative to [FragmentScenario] that allows tests to do work before launching the [Fragment]
 * (like switch out dependencies, construct intents etc) and also allows creation of multiple
 * scenarios in a test.
 */
class FragmentScenarioLauncherRule @JvmOverloads constructor(
    @StyleRes private val defaultThemeResId: Int? = null,
    private val defaultFactory: FragmentFactory? = null
) : ExternalResource() {

    private val scenarios = mutableListOf<FragmentScenario<*>>()

    fun <F : Fragment> launch(fragmentClass: Class<F>): FragmentScenario<F> {
        return FragmentScenario.launch(fragmentClass).also {
            scenarios.add(it)
        }
    }

    @JvmOverloads
    fun <F : Fragment> launchInContainer(
        fragmentClass: Class<F>,
        @StyleRes themResId: Int? = defaultThemeResId,
        factory: FragmentFactory? = defaultFactory
    ): FragmentScenario<F> {
        val scenario = if (themResId != null) {
            FragmentScenario.launchInContainer(
                fragmentClass,
                themeResId = themResId,
                factory = factory
            )
        } else {
            FragmentScenario.launchInContainer(fragmentClass, factory = factory)
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
