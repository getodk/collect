package org.odk.collect.androidshared.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

/**
 * Convenience object for creating [FragmentFactory] instances without needing to use an inner,
 * private or anonymous class.
 */
class FragmentFactoryBuilder {

    private val classFactories = mutableMapOf<Class<out Fragment>, () -> Fragment>()

    fun forClass(fragmentClass: Class<out Fragment>, factory: () -> Fragment): FragmentFactoryBuilder {
        classFactories[fragmentClass] = factory
        return this
    }

    fun build(): FragmentFactory {
        return object : androidx.fragment.app.FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                val fragmentClass = loadFragmentClass(classLoader, className)
                val factory = classFactories[fragmentClass]
                return if (factory != null) {
                    factory()
                } else {
                    super.instantiate(classLoader, className)
                }
            }
        }
    }
}
