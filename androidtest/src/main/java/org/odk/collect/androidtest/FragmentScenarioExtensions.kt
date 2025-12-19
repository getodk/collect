package org.odk.collect.androidtest

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.testing.FragmentScenario

object FragmentScenarioExtensions {

    fun <T : Fragment> FragmentScenario<T>.setFragmentResultListener(
        requestKey: String,
        listener: FragmentResultListener
    ) {
        onFragment {
            it.parentFragmentManager.setFragmentResultListener(requestKey, it, listener)
        }
    }
}
