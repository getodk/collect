package org.odk.collect.androidshared.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import kotlin.reflect.KClass

/**
 * Convenience object for creating [FragmentFactory] instances without needing to use an inner,
 * private or anonymous class.
 */
class FragmentFactoryBuilder {

    private val classesAndFactories = mutableListOf<Pair<Class<*>, () -> Fragment>>()

    fun forClass(fragmentClass: KClass<*>, factory: () -> Fragment): FragmentFactoryBuilder {
        return forClass(fragmentClass.java, factory)
    }

    fun forClass(fragmentClass: Class<*>, factory: () -> Fragment): FragmentFactoryBuilder {
        classesAndFactories.add(Pair(fragmentClass, factory))
        return this
    }

    fun build(): FragmentFactory {
        return object : androidx.fragment.app.FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                val fragmentClass = loadFragmentClass(classLoader, className)

                val factory =
                    classesAndFactories.find { it.first.isAssignableFrom(fragmentClass) }?.second

                return if (factory != null) {
                    factory()
                } else {
                    super.instantiate(classLoader, className)
                }
            }
        }
    }
}
