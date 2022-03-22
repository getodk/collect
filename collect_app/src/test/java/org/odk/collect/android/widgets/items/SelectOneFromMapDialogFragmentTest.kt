package org.odk.collect.android.widgets.items

import android.content.Context
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.databinding.SelectOneFromMapDialogLayoutBinding
import org.odk.collect.android.geo.MapProvider
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.SelectionMapFragment
import org.odk.collect.geo.maps.MapFragment
import org.odk.collect.geo.maps.MapFragmentFactory

@RunWith(AndroidJUnit4::class)
class SelectOneFromMapDialogFragmentTest {

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesMapFragmentFactory(mapProvider: MapProvider): MapFragmentFactory {
                return object : MapFragmentFactory {
                    override fun createMapFragment(context: Context): MapFragment? {
                        return null
                    }
                }
            }
        })
    }

    @Test
    fun `pressing back dismisses dialog`() {
        val scenario = launcherRule.launchDialogFragment(SelectOneFromMapDialogFragment::class.java)
        scenario.onFragment {
            Espresso.pressBack()
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `contains SelectionMapFragment`() {
        val scenario = launcherRule.launchDialogFragment(SelectOneFromMapDialogFragment::class.java)
        scenario.onFragment {
            val binding = SelectOneFromMapDialogLayoutBinding.bind(it.view!!)
            assertThat(binding.selectionMap.getFragment<SelectionMapFragment>(), notNullValue())
        }
    }
}
