package org.odk.collect.android.widgets.items

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.javarosa.core.model.SelectChoice
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.databinding.SelectOneFromMapDialogLayoutBinding
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.geo.MapProvider
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.widgets.items.SelectOneFromMapDialogFragment.Companion.ARG_FORM_INDEX
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.MappableSelectItem
import org.odk.collect.geo.SelectionMapFragment
import org.odk.collect.geo.maps.MapFragment
import org.odk.collect.geo.maps.MapFragmentFactory

@RunWith(AndroidJUnit4::class)
class SelectOneFromMapDialogFragmentTest {

    private val prompt by lazy {
        MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(
                listOf(
                    SelectChoice(
                        null,
                        "A",
                        "a",
                        false,
                        TreeElement("").also { item ->
                            item.addChild(
                                TreeElement("geometry").also {
                                    it.value = StringData("12.0 -1.0 305 0")
                                }
                            )
                        }
                    ),
                    SelectChoice(
                        null,
                        "B",
                        "b",
                        false,
                        TreeElement("").also { item ->
                            item.addChild(
                                TreeElement("geometry").also {
                                    it.value = StringData("13.0 -1.0 305 0")
                                }
                            )
                        }
                    )
                )
            )
            .build()
    }

    private val formEntryViewModel = mock<FormEntryViewModel> {
        on { getQuestionPrompt(prompt.index) } doReturn prompt
    }

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

            override fun providesFormEntryViewModelFactory(analytics: Analytics): FormEntryViewModel.Factory {
                return object : FormEntryViewModel.Factory(System::currentTimeMillis) {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return formEntryViewModel as T
                    }
                }
            }
        })
    }

    @Test
    fun `pressing back dismisses dialog`() {
        launcherRule.launchDialogFragment(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
            }
        ).onFragment {
            Espresso.pressBack()
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `contains SelectionMapFragment with correct data`() {
        launcherRule.launchDialogFragment(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
            }
        ).onFragment {
            val binding = SelectOneFromMapDialogLayoutBinding.bind(it.view!!)
            val fragment = binding.selectionMap.getFragment<SelectionMapFragment>()
            assertThat(fragment, notNullValue())

            val data = fragment.selectionMapData
            assertThat(data.getMapTitle().value, equalTo(prompt.longText))
            assertThat(data.getItemCount().value, equalTo(prompt.selectChoices.size))
            assertThat(
                data.getMappableItems().value,
                equalTo(
                    listOf(
                        MappableSelectItem.WithInfo(
                            0,
                            12.0,
                            -1.0,
                            R.drawable.ic_map_marker_24dp,
                            R.drawable.ic_map_marker_48dp,
                            "A",
                            MappableSelectItem.IconifiedText(R.drawable.ic_map_marker_24dp, ""),
                            ""
                        ),
                        MappableSelectItem.WithInfo(
                            1,
                            13.0,
                            -1.0,
                            R.drawable.ic_map_marker_24dp,
                            R.drawable.ic_map_marker_48dp,
                            "B",
                            MappableSelectItem.IconifiedText(R.drawable.ic_map_marker_24dp, ""),
                            ""
                        )
                    )
                )
            )
        }
    }
}
