package org.odk.collect.android.widgets.utilities

import android.R
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.GeoPointData
import org.javarosa.core.model.data.GeoShapeData
import org.javarosa.core.model.data.GeoTraceData
import org.javarosa.form.api.FormEntryController
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.javarosawrapper.FailedValidationResult
import org.odk.collect.android.javarosawrapper.SuccessValidationResult
import org.odk.collect.android.javarosawrapper.ValidationResult
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.geo.GeoPointMapDialogFragment
import org.odk.collect.android.widgets.geo.GeoPolyDialogFragment
import org.odk.collect.android.widgets.geo.ReferenceGeometryMappableData
import org.odk.collect.android.widgets.items.GeoSelectChoiceElements
import org.odk.collect.android.widgets.support.FormElementFixtures.selectChoice
import org.odk.collect.android.widgets.support.FormElementFixtures.treeElement
import org.odk.collect.android.widgets.utilities.AdditionalAttributes.INCREMENTAL
import org.odk.collect.android.widgets.utilities.WidgetAnswerDialogFragment.Companion.ARG_FORM_INDEX
import org.odk.collect.android.widgets.viewmodels.QuestionViewModel
import org.odk.collect.androidshared.ui.DisplayString
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.GeoUtils.toMapPoint
import org.odk.collect.geo.geopoint.GeoPointMapFragment
import org.odk.collect.geo.geopoly.GeoPolyFragment
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode
import org.odk.collect.geo.items.MappableItem
import org.odk.collect.maps.MapPoint
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.getOrAwaitValue

@RunWith(AndroidJUnit4::class)
class GeoPointMapDialogFragmentTest {

    private var prompt = MockFormEntryPromptBuilder().build()
    private val formEntryViewModel = mock<FormEntryViewModel> {
        on { getQuestionPrompt(prompt.index) } doReturn prompt
    }

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
                    GeoPointMapDialogFragment(viewModelFactory)
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
}
