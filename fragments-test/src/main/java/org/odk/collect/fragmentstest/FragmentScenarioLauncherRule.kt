package org.odk.collect.fragmentstest

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import com.google.android.material.R
import org.junit.rules.ExternalResource
import kotlin.reflect.KClass

/**
 * Alternative to [FragmentScenario] that allows tests to do work before launching the [Fragment]
 * (like switch out dependencies, construct intents etc) and also allows creation of multiple
 * scenarios in a test.
 */
class FragmentScenarioLauncherRule @JvmOverloads constructor(
    private val defaultFactory: FragmentFactory? = null,
    @StyleRes private val defaultThemeResId: Int = R.style.Theme_Material3_Light
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
                fragmentClass = fragmentClass,
                fragmentArgs = args,
                themeResId = themResId,
                factory = factory,
                initialState = initialState
            )
        } else {
            FragmentScenario.launchInContainer(
                fragmentClass = fragmentClass,
                fragmentArgs = args,
                factory = factory,
                initialState = initialState
            )
        }

        return scenario.also {
            scenarios.add(it)
        }
    }

    inline fun <reified F : Fragment> launchInContainer(crossinline factory: () -> F): FragmentScenario<F> {
        val fragmentFactory = object : FragmentFactory() {
            override fun instantiate(
                classLoader: ClassLoader,
                className: String
            ): Fragment {
                val fragmentClass = loadFragmentClass(classLoader, className)

                return if (F::class.java.isAssignableFrom(fragmentClass)) {
                    factory()
                } else {
                    super.instantiate(classLoader, className)
                }
            }
        }

        return launchInContainer(F::class.java, factory = fragmentFactory)
    }

    @JvmOverloads
    fun <F : Fragment> launch(
        fragmentClass: Class<F>,
        fragmentArgs: Bundle? = null,
        initialState: Lifecycle.State = Lifecycle.State.RESUMED
    ): FragmentScenario<F> {
        val scenario = if (defaultThemeResId != null) {
            FragmentScenario.launch(
                fragmentClass = fragmentClass,
                fragmentArgs = fragmentArgs,
                themeResId = defaultThemeResId,
                factory = defaultFactory,
                initialState = initialState
            )
        } else {
            FragmentScenario.launch(
                fragmentClass = fragmentClass,
                fragmentArgs = fragmentArgs,
                factory = defaultFactory,
                initialState = initialState
            )
        }

        return scenario.also {
            scenarios.add(it)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <Child : Fragment> launchAndAssertOnChild(
        fragment: KClass<out Fragment>,
        args: Bundle,
        assertion: (Child) -> Unit
    ) {
        launch(
            fragment.java,
            args
        ).onFragment {
            assertion(it.childFragmentManager.fragments[0] as Child)
        }
    }

    override fun after() {
        scenarios.forEach(FragmentScenario<*>::close)
    }
}
