package org.odk.collect.android.widgets.utilities

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.StringData
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
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.widgets.utilities.WidgetAnswerDialogFragment.Companion.ARG_FORM_INDEX
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.geopoly.GeoPolyFragment
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode
import org.odk.collect.maps.MapPoint
import kotlin.reflect.KClass

@RunWith(AndroidJUnit4::class)
class GeoPolyDialogFragmentTest {

    private var prompt = MockFormEntryPromptBuilder().build()
    private val formEntryViewModel = mock<FormEntryViewModel> {
        on { getQuestionPrompt(prompt.index) } doReturn prompt
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
        prompt = MockFormEntryPromptBuilder(prompt).withReadOnly(true).build()
        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(GeoPolyDialogFragment::class) {
            assertThat(it.readOnly, equalTo(true))
        }

        prompt = MockFormEntryPromptBuilder(prompt).withReadOnly(false).build()
        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(GeoPolyDialogFragment::class) {
            assertThat(it.readOnly, equalTo(false))
        }
    }

    @Test
    fun `configures GeoPolyFragment with geoshape output mode when prompt is geoshape`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOSHAPE)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(GeoPolyDialogFragment::class) {
            assertThat(it.outputMode, equalTo(OutputMode.GEOSHAPE))
        }
    }

    @Test
    fun `configures GeoPolyFragment with geotrace output mode when prompt is geotrace`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_GEOTRACE)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(GeoPolyDialogFragment::class) {
            assertThat(it.outputMode, equalTo(OutputMode.GEOTRACE))
        }
    }

    @Test
    fun `configures GeoPolyFragment with null output mode when prompt is something else`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withDataType(Constants.DATATYPE_DATE)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(GeoPolyDialogFragment::class) {
            assertThat(it.outputMode, equalTo(null))
        }
    }

    @Test
    fun `configures GeoPolyFragment with retainMockAccruacy from allow-mock-accuracy bind attribute`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(GeoPolyDialogFragment::class) {
            assertThat(it.retainMockAccuracy, equalTo(false))
        }

        prompt = MockFormEntryPromptBuilder(prompt)
            .withBindAttribute(null, "allow-mock-accuracy", "true")
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(GeoPolyDialogFragment::class) {
            assertThat(it.retainMockAccuracy, equalTo(true))
        }

        prompt = MockFormEntryPromptBuilder(prompt)
            .withBindAttribute(null, "allow-mock-accuracy", "false")
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(GeoPolyDialogFragment::class) {
            assertThat(it.retainMockAccuracy, equalTo(false))
        }
    }

    @Test
    fun `configures GeoPolyFragment inputPolgyon with existing answer`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(GeoPolyDialogFragment::class) {
            assertThat(it.inputPolygon, equalTo(null))
        }

        prompt = MockFormEntryPromptBuilder(prompt)
            .withAnswer(StringData("0.0 0.0 1.0 1.0; 0.0 1.0 1.0 1.0"))
            .build()

        launcherRule.launchAndAssertOnChild<GeoPolyFragment>(GeoPolyDialogFragment::class) {
            assertThat(
                it.inputPolygon,
                equalTo(listOf(MapPoint(0.0, 0.0, 1.0, 1.0), MapPoint(0.0, 1.0, 1.0, 1.0)))
            )
        }
    }

    @Test
    fun `sets answer when REQUEST_GEOPOLY is returned`() {
        prompt = MockFormEntryPromptBuilder(prompt)
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

        verify(formEntryViewModel).answerQuestion(prompt.index, StringData(answer))
    }

    @Test
    fun `dismisses when REQUEST_GEOPOLY is returned`() {
        prompt = MockFormEntryPromptBuilder(prompt)
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

            assertThat(it.dialog!!.isShowing, equalTo(false))
        }
    }

    @Test
    fun `does not set answer when REQUEST_GEOPOLY is cancelled`() {
        prompt = MockFormEntryPromptBuilder(prompt)
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
            .build()

        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            it.childFragmentManager.setFragmentResult(GeoPolyFragment.REQUEST_GEOPOLY, Bundle.EMPTY)
            assertThat(it.dialog!!.isShowing, equalTo(false))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Fragment> FragmentScenarioLauncherRule.launchAndAssertOnChild(
        fragment: KClass<out Fragment>, assertion: (T) -> Unit
    ) {
        this.launch(
            fragment.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            assertion(it.childFragmentManager.fragments[0] as T)
        }
    }
}
