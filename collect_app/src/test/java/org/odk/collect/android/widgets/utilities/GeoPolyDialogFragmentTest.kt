package org.odk.collect.android.widgets.utilities

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.Constants
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.widgets.utilities.WidgetAnswerDialogFragment.Companion.ARG_FORM_INDEX
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.geo.geopoly.GeoPolyFragment
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode

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
        launchAndAssertOnGeoPolyFragment {
            assertThat(it.readOnly, equalTo(true))
        }

        prompt = MockFormEntryPromptBuilder(prompt).withReadOnly(false).build()
        launchAndAssertOnGeoPolyFragment {
            assertThat(it.readOnly, equalTo(false))
        }
    }

    @Test
    fun `configures GeoPolyFragment with geoshape output mode when prompt is geoshape`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withControlType(Constants.DATATYPE_GEOSHAPE)
            .build()

        launchAndAssertOnGeoPolyFragment {
            assertThat(it.outputMode, equalTo(OutputMode.GEOSHAPE))
        }
    }

    @Test
    fun `configures GeoPolyFragment with geotrace output mode when prompt is geotrace`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withControlType(Constants.DATATYPE_GEOTRACE)
            .build()

        launchAndAssertOnGeoPolyFragment {
            assertThat(it.outputMode, equalTo(OutputMode.GEOTRACE))
        }
    }

    @Test
    fun `configures GeoPolyFragment with null output mode when prompt is something else`() {
        prompt = MockFormEntryPromptBuilder(prompt)
            .withControlType(Constants.DATATYPE_DATE)
            .build()

        launchAndAssertOnGeoPolyFragment {
            assertThat(it.outputMode, equalTo(null))
        }
    }

    private fun launchAndAssertOnGeoPolyFragment(assertion: (GeoPolyFragment) -> Unit) {
        launcherRule.launch(
            GeoPolyDialogFragment::class.java,
            bundleOf(ARG_FORM_INDEX to prompt.index)
        ).onFragment {
            val geoPolyFragment = it.childFragmentManager.fragments[0] as GeoPolyFragment
            assertion(geoPolyFragment)
        }
    }
}
