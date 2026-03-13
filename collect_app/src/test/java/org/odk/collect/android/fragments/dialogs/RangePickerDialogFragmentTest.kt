package org.odk.collect.android.fragments.dialogs

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.javarosa.core.model.data.IntegerData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.R
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.fragments.dialogs.RangePickerDialogFragment.Companion.ARG_FORM_INDEX
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.ViewActions.setNumber
import org.odk.collect.testshared.ViewMatchers.hasPicked

@RunWith(AndroidJUnit4::class)
class RangePickerDialogFragmentTest {

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
                .forClass(RangePickerDialogFragment::class) {
                    RangePickerDialogFragment(viewModelFactory)
                }.build()
        )

    @Test
    fun `answers question when number selected`() {
        launcherRule.launch(
            RangePickerDialogFragment::class.java,
            bundleOf(
                ARG_FORM_INDEX to prompt.index,
                RangePickerDialogFragment.ARG_VALUES to arrayOf("1", "2", "3"),
                RangePickerDialogFragment.ARG_SELECTED to 0
            )
        )

        onView(withId(R.id.number_picker)).inRoot(isDialog()).perform(setNumber("2"))
        onView(withText(string.ok)).perform(click())

        verify(formEntryViewModel).answerQuestion(prompt.index, IntegerData(2))
    }

    @Test
    fun `selects value based on VALUE arg`() {
        launcherRule.launch(
            RangePickerDialogFragment::class.java,
            bundleOf(
                ARG_FORM_INDEX to prompt.index,
                RangePickerDialogFragment.ARG_VALUES to arrayOf("1", "2", "3"),
                RangePickerDialogFragment.ARG_SELECTED to 1
            )
        )

        onView(withId(R.id.number_picker)).inRoot(isDialog()).check(matches(hasPicked("2")))
    }
}