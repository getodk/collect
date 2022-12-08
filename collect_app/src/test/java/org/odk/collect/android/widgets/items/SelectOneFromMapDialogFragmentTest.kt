package org.odk.collect.android.widgets.items

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.javarosa.core.model.data.SelectOneData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.databinding.SelectOneFromMapDialogLayoutBinding
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.formentry.FormSessionRepository
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.items.SelectOneFromMapDialogFragment.Companion.ARG_FORM_INDEX
import org.odk.collect.android.widgets.items.SelectOneFromMapDialogFragment.Companion.ARG_SELECTED_INDEX
import org.odk.collect.android.widgets.support.FormFixtures.selectChoice
import org.odk.collect.android.widgets.support.FormFixtures.treeElement
import org.odk.collect.android.widgets.support.NoOpMapFragment
import org.odk.collect.async.Scheduler
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.selection.MappableSelectItem
import org.odk.collect.geo.selection.MappableSelectItem.IconifiedText
import org.odk.collect.geo.selection.SelectionMapFragment
import org.odk.collect.geo.selection.SelectionMapFragment.Companion.REQUEST_SELECT_ITEM
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class SelectOneFromMapDialogFragmentTest {

    private val selectChoices = listOf(
        selectChoice(
            value = "a",
            item = treeElement(children = listOf(treeElement("geometry", "12.0 -1.0 305 0")))
        ),
        selectChoice(
            value = "b",
            item = treeElement(children = listOf(treeElement("geometry", "13.0 -1.0 305 0")))
        )
    )

    private val prompt = MockFormEntryPromptBuilder()
        .withLongText("Which is your favourite place?")
        .withSelectChoices(
            selectChoices
        )
        .withSelectChoiceText(
            mapOf(
                selectChoices[0] to "A",
                selectChoices[1] to "B"
            )
        )
        .build()

    private val formEntryViewModel = mock<FormEntryViewModel> {
        on { getQuestionPrompt(prompt.index) } doReturn prompt
    }

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val scheduler = FakeScheduler()

    @get:Rule
    val launcherRule =
        FragmentScenarioLauncherRule(defaultThemeResId = R.style.Theme_MaterialComponents)

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesMapFragmentFactory(settingsProvider: SettingsProvider): MapFragmentFactory {
                return object : MapFragmentFactory {
                    override fun createMapFragment(): MapFragment {
                        return NoOpMapFragment()
                    }
                }
            }

            override fun providesFormEntryViewModelFactory(scheduler: Scheduler, formSessionStore: FormSessionRepository): FormEntryViewModel.Factory {
                return object : FormEntryViewModel.Factory(System::currentTimeMillis, scheduler, formSessionStore) {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return formEntryViewModel as T
                    }
                }
            }

            override fun providesScheduler(workManager: WorkManager?): Scheduler {
                return scheduler
            }
        })
    }

    @Test
    fun `pressing back dismisses dialog`() {
        val scenario = launcherRule.launch(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
            }
        )

        scheduler.runBackground()

        scenario.onFragment {
            Espresso.pressBack()
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `sets up SelectionMapFragment`() {
        val scenario = launcherRule.launch(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
            }
        )

        scenario.onFragment {
            val binding = SelectOneFromMapDialogLayoutBinding.bind(it.view!!)
            val fragment = binding.selectionMap.getFragment<SelectionMapFragment>()
            assertThat(fragment, notNullValue())
            assertThat(fragment.skipSummary, equalTo(false))
            assertThat(fragment.showNewItemButton, equalTo(false))

            val dialogBackPressedDispatcher =
                (it.requireDialog() as ComponentDialog).onBackPressedDispatcher
            assertThat(fragment.onBackPressedDispatcher?.invoke(), equalTo(dialogBackPressedDispatcher))
        }
    }

    @Test
    fun `gives SelectionMapFragment correct data`() {
        val scenario = launcherRule.launch(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
            }
        )

        scenario.onFragment {
            val binding = SelectOneFromMapDialogLayoutBinding.bind(it.view!!)
            val fragment = binding.selectionMap.getFragment<SelectionMapFragment>()

            val data = fragment.selectionMapData
            scheduler.runBackground()

            assertThat(data.getMapTitle().value, equalTo(prompt.longText))
            assertThat(data.getItemCount().value, equalTo(prompt.selectChoices.size))
            assertThat(
                data.getMappableItems().value,
                equalTo(
                    listOf(
                        MappableSelectItem.WithAction(
                            0,
                            selectChoices[0].getChild("geometry")!!.split(" ")[0].toDouble(),
                            selectChoices[0].getChild("geometry")!!.split(" ")[1].toDouble(),
                            R.drawable.ic_map_marker_with_hole_small,
                            R.drawable.ic_map_marker_with_hole_big,
                            "A",
                            emptyList(),
                            IconifiedText(
                                R.drawable.ic_save, application.getString(R.string.select_item)
                            )
                        ),
                        MappableSelectItem.WithAction(
                            1,
                            selectChoices[1].getChild("geometry")!!.split(" ")[0].toDouble(),
                            selectChoices[1].getChild("geometry")!!.split(" ")[1].toDouble(),
                            R.drawable.ic_map_marker_with_hole_small,
                            R.drawable.ic_map_marker_with_hole_big,
                            "B",
                            emptyList(),
                            IconifiedText(
                                R.drawable.ic_save,
                                application.getString(R.string.select_item)
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `contains SelectionMapFragment with correct data with selected index`() {
        val scenario = launcherRule.launch(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
                it.putSerializable(ARG_SELECTED_INDEX, selectChoices[1].index)
            }
        )

        scheduler.runBackground()

        scenario.onFragment {
            val binding = SelectOneFromMapDialogLayoutBinding.bind(it.view!!)
            val fragment = binding.selectionMap.getFragment<SelectionMapFragment>()
            assertThat(fragment, notNullValue())
            assertThat(fragment.skipSummary, equalTo(false))
            assertThat(fragment.showNewItemButton, equalTo(false))

            val data = fragment.selectionMapData
            assertThat(data.getMappableItems().value!![1].selected, equalTo(true))
        }
    }

    @Test
    fun `contains SelectionMapFragment with correct data for quick appearance`() {
        val prompt = MockFormEntryPromptBuilder()
            .withAppearance("${Appearances.MAP} ${Appearances.QUICK}")
            .build()
        whenever(formEntryViewModel.getQuestionPrompt(prompt.index)).thenReturn(prompt)

        val scenario = launcherRule.launch(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
            }
        )

        scheduler.runBackground()

        scenario.onFragment {
            val binding = SelectOneFromMapDialogLayoutBinding.bind(it.view!!)
            val fragment = binding.selectionMap.getFragment<SelectionMapFragment>()
            assertThat(fragment.skipSummary, equalTo(true))
        }
    }

    @Test
    fun `selecting a choice on the map answers question and dismisses`() {
        val scenario = launcherRule.launch(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
            }
        )

        scheduler.runBackground()

        scenario.onFragment {
            val result = bundleOf(SelectionMapFragment.RESULT_SELECTED_ITEM to 1L)
            it.childFragmentManager.setFragmentResult(REQUEST_SELECT_ITEM, result)
            assertThat(it.isVisible, equalTo(false))
        }

        verify(formEntryViewModel).answerQuestion(
            prompt.index,
            SelectOneData(selectChoices[1].selection())
        )
    }
}
