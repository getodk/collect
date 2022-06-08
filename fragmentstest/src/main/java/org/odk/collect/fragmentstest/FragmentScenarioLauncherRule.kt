package org.odk.collect.fragmentstest

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
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

    @JvmOverloads
    fun <F : Fragment> launchInContainer(
        fragmentClass: Class<F>,
        args: Bundle? = null,
        @StyleRes themResId: Int? = defaultThemeResId,
        factory: FragmentFactory? = defaultFactory,
        initialState: Lifecycle.State = Lifecycle.State.RESUMED
    ): FragmentScenario<F> {
        val scenario = if (themResId != null) {
            FragmentScenario.launchInContainer(
                fragmentClass,
                fragmentArgs = args,
                themeResId = themResId,
                factory = factory,
                initialState = initialState
            )
        } else {
            FragmentScenario.launchInContainer(
                fragmentClass,
                fragmentArgs = args,
                factory = factory,
                initialState = initialState
            )
        }

        return scenario.also {
            scenarios.add(it)
        }
    }

    @JvmOverloads
    fun <F : Fragment> launch(
        fragmentClass: Class<F>,
        fragmentArgs: Bundle? = null
    ): FragmentScenario<F> {
        val scenario = if (defaultThemeResId != null) {
            FragmentScenario.launch(
                fragmentClass,
                fragmentArgs,
                defaultThemeResId,
                null
            )
        } else {
            FragmentScenario.launch(
                fragmentClass,
                fragmentArgs,
                null
            )
        }

        return scenario.also {
            scenarios.add(it)
        }
    }

    override fun after() {
        scenarios.forEach(FragmentScenario<*>::close)
    }
}
