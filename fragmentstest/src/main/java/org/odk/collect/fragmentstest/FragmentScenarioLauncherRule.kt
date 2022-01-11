package org.odk.collect.fragmentstest

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import org.junit.rules.ExternalResource

/**
 * Alternative to [FragmentScenario] that allows tests to do work before launching the [Fragment]
 * (like switch out dependencies, construct intents etc) and also allows creation of multiple
 * scenarios in a test.
 */
class FragmentScenarioLauncherRule : ExternalResource() {

    private val scenarios = mutableListOf<FragmentScenario<*>>()

    fun <F : Fragment> launch(fragmentClass: Class<F>): FragmentScenario<F> {
        return FragmentScenario.launch(fragmentClass)
    }

    override fun after() {
        scenarios.forEach(FragmentScenario<*>::close)
    }
}
