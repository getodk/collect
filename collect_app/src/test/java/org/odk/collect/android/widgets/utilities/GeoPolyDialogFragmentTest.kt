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
import org.javarosa.core.model.Constants
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.data.GeoShapeData
import org.javarosa.core.model.data.GeoTraceData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.javarosawrapper.FailedValidationResult
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.widgets.utilities.AdditionalAttributes.INCREMENTAL
import org.odk.collect.android.widgets.utilities.WidgetAnswerDialogFragment.Companion.ARG_FORM_INDEX
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.geopoly.GeoPolyFragment
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode
import org.odk.collect.maps.MapPoint
import org.odk.collect.testshared.getOrAwaitValue

@RunWith(AndroidJUnit4::class)
class GeoPolyDialogFragmentTest {

    private var prompt = MockFormEntryPromptBuilder().build()
    private val index =
        MutableLiveData<Pair<FormIndex, FailedValidationResult?>>(Pair(prompt.index, null))
    private val formEntryViewModel = mock<FormEntryViewModel> {
        on { getQuestionPrompt(prompt.index) } doReturn prompt
        on { currentIndex } doReturn index
    }

    private val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return formEntryViewModel as T
        }
    }

    @get:Rule
    val launcherRule =
        FragmentScenarioLauncherRule(
            FragmentFactoryBuilder()
                .forClass(GeoPolyDialogFragment::class) {
                    GeoPolyDialogFragment(viewModelFactory)
                }.build()
        )

    @Before
    fun setup() {
        CollectHelpers.setupDemoProject()
    }

    @Test
    fun `configures GeoPolyFragment with readOnly from prompt`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .withReadOnly(true)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.readOnly, equalTo(true))
        }

        prompt = MockFormEntryPromptBuilder(prompt).withReadOnly(false).build()
        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.readOnly, equalTo(false))
        }
    }

    @Test
    fun `configures GeoPolyFragment with geoshape output mode when prompt is geoshape`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOSHAPE)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.outputMode, equalTo(OutputMode.GEOSHAPE))
        }
    }

    @Test
    fun `configures GeoPolyFragment with geotrace output mode when prompt is geotrace`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.outputMode, equalTo(OutputMode.GEOTRACE))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception when prompt is something else`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_DATE)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.outputMode, equalTo(OutputMode.GEOTRACE))
        }
    }

    @Test
    fun `configures GeoPolyFragment with retainMockAccruacy from allow-mock-accuracy bind attribute`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.retainMockAccuracy, equalTo(false))
        }

        prompt = MockFormEntryPromptBuilder(prompt)
            .withBindAttribute(null, "allow-mock-accuracy", "true")
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.retainMockAccuracy, equalTo(true))
        }

        prompt = MockFormEntryPromptBuilder(prompt)
            .withBindAttribute(null, "allow-mock-accuracy", "false")
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.retainMockAccuracy, equalTo(false))
        }
    }

    @Test
    fun `configures GeoPolyFragment inputPolygon with existing answer`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.inputPolygon, equalTo(emptyList()))
        }

        val points = listOf(MapPoint(0.0, 0.0, 1.0, 1.0), MapPoint(0.0, 1.0, 1.0, 1.0))
        prompt = MockFormEntryPromptBuilder(prompt)
            .withAnswer(geoTraceOf(points))
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.inputPolygon, equalTo(points))
        }

        prompt = MockFormEntryPromptBuilder(prompt)
            .withAnswer(geoShapeOf(points))
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.inputPolygon, equalTo(points))
        }
    }

    @Test
    fun `sets answer when REQUEST_GEOPOLY is returned`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .build()

        val answer = "0.0 0.0 1.0 1.0; 0.0 1.0 1.0 1.0"
        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            it.childFragmentManager.setFragmentResult(
                GeoPolyFragment.REQUEST_GEOPOLY,
                bundleOf(GeoPolyFragment.RESULT_GEOPOLY to answer)
            )
        }

        verify(formEntryViewModel).answerQuestion(prompt.index, geoTraceOf(answer), false)
    }

    @Test
    fun `sets GeoShapeData answer when REQUEST_GEOPOLY is returned for GEOSHAPE prompt`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOSHAPE)
            .build()

        val answer = "0.0 0.0 1.0 1.0; 0.0 1.0 1.0 1.0; 1.0 1.0 0.0 0.0"
        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            it.childFragmentManager.setFragmentResult(
                GeoPolyFragment.REQUEST_GEOPOLY,
                bundleOf(GeoPolyFragment.RESULT_GEOPOLY to answer)
            )
        }

        verify(formEntryViewModel).answerQuestion(prompt.index, geoShapeOf(answer), false)
    }

    @Test
    fun `dismisses when REQUEST_GEOPOLY is returned`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .build()

        val answer = "0.0 0.0 1.0 1.0; 0.0 1.0 1.0 1.0"
        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            assertThat(it.dialog!!.isShowing, equalTo(true))

            it.childFragmentManager.setFragmentResult(
                GeoPolyFragment.REQUEST_GEOPOLY,
                bundleOf(GeoPolyFragment.RESULT_GEOPOLY to answer)
            )

            assertThat(it.dialog!!.isShowing, equalTo(false))
        }
    }

    @Test
    fun `sets answer with validate when REQUEST_GEOPOLY_CHANGE is returned if question is incremental`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .withAdditionalAttribute(INCREMENTAL, "true")
            .build()

        val answer = "0.0 0.0 1.0 1.0; 0.0 1.0 1.0 1.0"
        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            it.childFragmentManager.setFragmentResult(
                GeoPolyFragment.REQUEST_GEOPOLY,
                bundleOf(GeoPolyFragment.RESULT_GEOPOLY_CHANGE to answer)
            )
        }

        verify(formEntryViewModel).answerQuestion(prompt.index, geoTraceOf(answer), true)
    }

    @Test
    fun `sets GeoShapeData answer with validate when REQUEST_GEOPOLY_CHANGE is returned if GEOSHAPE question is incremental`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withAdditionalAttribute(INCREMENTAL, "true")
            .withDataType(Constants.DATATYPE_GEOSHAPE)
            .build()

        val answer = "0.0 0.0 1.0 1.0; 0.0 1.0 1.0 1.0"
        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            it.childFragmentManager.setFragmentResult(
                GeoPolyFragment.REQUEST_GEOPOLY,
                bundleOf(GeoPolyFragment.RESULT_GEOPOLY_CHANGE to answer)
            )
        }

        verify(formEntryViewModel).answerQuestion(prompt.index, geoShapeOf(answer), true)
    }

    @Test
    fun `does not set answer when REQUEST_GEOPOLY_CHANGE is returned if question is not incremental`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .build()

        val answer = "0.0 0.0 1.0 1.0; 0.0 1.0 1.0 1.0"
        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            it.childFragmentManager.setFragmentResult(
                GeoPolyFragment.REQUEST_GEOPOLY,
                bundleOf(GeoPolyFragment.RESULT_GEOPOLY_CHANGE to answer)
            )
        }

        verify(formEntryViewModel, never()).answerQuestion(prompt.index, geoTraceOf(answer))

        prompt = MockFormEntryPromptBuilder(prompt)
            .withAdditionalAttribute(INCREMENTAL, "false")
            .build()

        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            it.childFragmentManager.setFragmentResult(
                GeoPolyFragment.REQUEST_GEOPOLY,
                bundleOf(GeoPolyFragment.RESULT_GEOPOLY_CHANGE to answer)
            )
        }

        verify(formEntryViewModel, never()).answerQuestion(prompt.index, geoTraceOf(answer))
    }

    @Test
    fun `does not dismiss when REQUEST_GEOPOLY_CHANGE is returned regardless of incremental value`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .withAdditionalAttribute(INCREMENTAL, "true")
            .build()

        val answer = "0.0 0.0 1.0 1.0; 0.0 1.0 1.0 1.0"
        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            assertThat(it.dialog!!.isShowing, equalTo(true))

            it.childFragmentManager.setFragmentResult(
                GeoPolyFragment.REQUEST_GEOPOLY,
                bundleOf(GeoPolyFragment.RESULT_GEOPOLY_CHANGE to answer)
            )

            assertThat(it.dialog!!.isShowing, equalTo(true))
        }

        prompt = MockFormEntryPromptBuilder(prompt)
            .withAdditionalAttribute(INCREMENTAL, "false")
            .build()

        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            it.childFragmentManager.setFragmentResult(
                GeoPolyFragment.REQUEST_GEOPOLY,
                bundleOf(GeoPolyFragment.RESULT_GEOPOLY_CHANGE to answer)
            )

            assertThat(it.dialog!!.isShowing, equalTo(true))
        }
    }

    @Test
    fun `does not set answer when REQUEST_GEOPOLY is cancelled`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .build()

        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            it.childFragmentManager.setFragmentResult(GeoPolyFragment.REQUEST_GEOPOLY, Bundle.EMPTY)
        }

        verify(formEntryViewModel, never()).answerQuestion(any(), any())
    }

    @Test
    fun `dismisses when REQUEST_GEOPOLY is cancelled`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .build()

        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            it.childFragmentManager.setFragmentResult(GeoPolyFragment.REQUEST_GEOPOLY, Bundle.EMPTY)
            assertThat(it.dialog!!.isShowing, equalTo(false))
        }
    }

    @Test
    fun `uses validation result message for invalidMessage`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .build()

        index.value = Pair(prompt.index, null)
        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.invalidMessage.getOrAwaitValue(), equalTo(null))
        }

        index.value = Pair(prompt.index, FailedValidationResult(prompt.index, 0, "blah", 0))
        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.invalidMessage.getOrAwaitValue(), equalTo("blah"))
        }
    }

    @Test
    fun `uses validation result default message for invalidMessage if there's no custom message`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .build()

        index.value = Pair(prompt.index, null)
        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.invalidMessage.getOrAwaitValue(), equalTo(null))
        }

        index.value =
            Pair(prompt.index, FailedValidationResult(prompt.index, 0, null, R.string.cancel))
        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(
            GeoPolyDialogFragment::class,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ) {
            assertThat(it.invalidMessage.getOrAwaitValue(), equalTo("Cancel"))
        }
    }

    private fun geoTraceOf(points: List<MapPoint>): GeoTraceData {
        return GeoTraceData(
            GeoTraceData.GeoTrace(
                ArrayList(
                    points.map {
                        doubleArrayOf(
                            it.latitude,
                            it.longitude,
                            it.altitude,
                            it.accuracy
                        )
                    }
                )
            )
        )
    }

    private fun geoShapeOf(points: List<MapPoint>): GeoShapeData {
        return GeoShapeData(
            GeoShapeData.GeoShape(
                ArrayList(
                    points.map {
                        doubleArrayOf(
                            it.latitude,
                            it.longitude,
                            it.altitude,
                            it.accuracy
                        )
                    }
                )
            )
        )
    }

    private fun geoTraceOf(answer: String): GeoTraceData {
        return GeoTraceData().also { it.value = answer }
    }

    private fun geoShapeOf(answer: String): GeoShapeData {
        return GeoShapeData().also { it.value = answer }
    }
}
