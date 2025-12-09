package org.odk.collect.testshared

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

/**
 * Instead of instantiate requested Fragments, will instantiate a [MockFragment] (or
 * [MockDialogFragment]) that wraps the Fragment class. This allows a test to check that the right
 * Fragments are setup without needing to deal with their actual creation/dependencies.
 */
class MockFragmentFactory : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        val fragmentClass = loadFragmentClass(classLoader, className)
        return if (DialogFragment::class.java.isAssignableFrom(fragmentClass)) {
            MockDialogFragment(fragmentClass)
        } else {
            MockFragment(fragmentClass)
        }
    }
}

class MockFragment(val fragmentClass: Class<out Fragment>) : Fragment()
class MockDialogFragment(val fragmentClass: Class<out Fragment>) : DialogFragment()
