package org.odk.collect.android.widgets.utilities

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.javarosa.core.model.data.GeoPointData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.geo.GeoPointMapDialogFragment
import org.odk.collect.android.widgets.geo.ReferenceGeometryMappableData
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements
import org.odk.collect.android.widgets.support.FormElementFixtures.selectChoice
import org.odk.collect.android.widgets.support.FormElementFixtures.treeElement
import org.odk.collect.android.widgets.utilities.WidgetAnswerDialogFragment.Companion.ARG_FORM_INDEX
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidtest.TestDispatcherProvider
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.GeoUtils.toMapPoint
import org.odk.collect.geo.geopoint.GeoPointMapFragment
import org.odk.collect.geo.items.MappableItem
import org.odk.collect.maps.MapPoint
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.getOrAwaitValue

@RunWith(AndroidJUnit4::class)
class GeoPointMapDialogFragmentTest {

    private var prompt = MockFormEntryPromptBuilder().build()
    private val formEntryViewModel = mock<FormEntryViewModel> {
        on { getQuestionPrompt(prompt.index) } doReturn prompt
        on { loadSelectChoices(prompt) } doAnswer { prompt.selectChoices }
    }

    private val dispatcherProvider = TestDispatcherProvider()

    private val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return when (modelClass) {
                FormEntryViewModel::class.java -> formEntryViewModel as T
                else -> throw IllegalArgumentException()
            }
        }
    }

    @get:Rule
    val launcherRule =
        FragmentScenarioLauncherRule(
            FragmentFactoryBuilder()
                .forClass(GeoPointMapDialogFragment::class) {
                    GeoPointMapDialogFragment(viewModelFactory, dispatcherProvider)
                }.build()
        )

    @Before
    fun setup() {
        CollectHelpers.setupDemoProject()
    }

    @Test
    fun `configures GeoPointMapFragment with answer`() {
        val answer = GeoPointData(doubleArrayOf(5.0, 6.0, 7.0, 8.0))
        prompt = MockFormEntryPromptBuilder(prompt)
            .withAnswer(answer)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPointMapFragment>(
            GeoPointMapDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.inputPoint, equalTo(answer.toMapPoint()))
        }
    }

    @Test
    fun `configures GeoPointMapFragment as not draggable for maps appearance`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withAppearance(Appearances.MAPS)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPointMapFragment>(
            GeoPointMapDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.draggable, equalTo(false))
        }
    }

    @Test
    fun `configures GeoPointMapFragment as draggable for placement map appearance`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withAppearance(Appearances.PLACEMENT_MAP)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPointMapFragment>(
            GeoPointMapDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.draggable, equalTo(true))
        }
    }

    @Test
    fun `configures GeoPointMapFragment as read only when prompt is`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withReadOnly(true)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPointMapFragment>(
            GeoPointMapDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.readOnly, equalTo(true))
        }
    }

    @Test
    fun `configures GeoPointMapFragment with MappableData`() {
        val selectChoices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement(
                            GeoSelectChoiceElements.GEOMETRY,
                            "12.0 -1.0 305 0"
                        )
                    )
                )
            ),
            selectChoice(
                value = "b",
                item = treeElement(
                    children = listOf(
                        treeElement(
                            GeoSelectChoiceElements.GEOMETRY,
                            "12.0 -1.0 3 4; 12.1 -1.0 3 4"
                        )
                    )
                )
            ),
            selectChoice(
                value = "c",
                item = treeElement(
                    children = listOf(
                        treeElement(
                            GeoSelectChoiceElements.GEOMETRY,
                            "12.0 -1.0 3 4; 12.1 -1.0 3 4; 12.0 -1.0 3 4"
                        )
                    )
                )
            )
        )

        prompt = MockFormEntryPromptBuilder(prompt)
            .withSelectChoices(selectChoices)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPointMapFragment>(
            GeoPointMapDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            dispatcherProvider.flush()
            assertThat(it.mappableData, notNullValue())
            val mappableItems = it.mappableData!!.getMappableItems().getOrAwaitValue()
            assertThat(mappableItems.size, equalTo(3))

            val point = mappableItems[0] as MappableItem.Point
            assertThat(point.point, equalTo(MapPoint(12.0, -1.0, 305.0)))
            assertThat(point.color, equalTo(ReferenceGeometryMappableData.ITEM_COLOR))

            val line = mappableItems[1] as MappableItem.Line
            assertThat(
                line.points,
                equalTo(listOf(MapPoint(12.0, -1.0, 3.0, 4.0), MapPoint(12.1, -1.0, 3.0, 4.0)))
            )
            assertThat(line.strokeColor, equalTo(ReferenceGeometryMappableData.ITEM_COLOR))

            val polygon = mappableItems[2] as MappableItem.Polygon
            assertThat(
                polygon.points,
                equalTo(
                    listOf(
                        MapPoint(12.0, -1.0, 3.0, 4.0),
                        MapPoint(12.1, -1.0, 3.0, 4.0),
                        MapPoint(12.0, -1.0, 3.0, 4.0)
                    )
                )
            )
            assertThat(polygon.strokeColor, equalTo(ReferenceGeometryMappableData.ITEM_COLOR))
            assertThat(polygon.fillColor, equalTo(ReferenceGeometryMappableData.ITEM_COLOR))
        }
    }
}
