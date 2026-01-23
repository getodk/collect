package org.odk.collect.android.widgets.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeReference
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.support.InMemFormSessionRepository
import org.odk.collect.android.javarosawrapper.FailedValidationResult
import org.odk.collect.android.javarosawrapper.FakeFormController
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.strings.R
import org.odk.collect.testshared.FakeScheduler
import org.odk.collect.testshared.getOrAwaitValue

@RunWith(AndroidJUnit4::class)
class QuestionViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val scheduler: FakeScheduler = FakeScheduler()
    private val startingIndex = FormIndex(null, 0, 0, TreeReference())
    private val formController = FakeFormController(startingIndex, mock())
    private val formSessionRepository = InMemFormSessionRepository().apply {
        set("blah", formController, mock())
    }
    private val viewModel = QuestionViewModel(scheduler, formSessionRepository, "blah")

    @Test
    fun `validate updates constraintValidationResult`() {
        val formDef: FormDef = mock()
        whenever(formDef.evaluateConstraint(any(), any())).thenReturn(false)
        formController.setFormDef(formDef)

        val reference = TreeReference()
        reference.add("blah", TreeReference.INDEX_UNBOUND)
        val formIndex = FormIndex(null, 1, 1, reference)
        val prompt = MockFormEntryPromptBuilder().build()
        formController.setPrompt(formIndex, prompt)

        val failedValidationResult =
            FailedValidationResult(formIndex, 0, null, R.string.invalid_answer_error)
        formController.setFailedConstraint(failedValidationResult)

        viewModel.validate(formIndex, StringData("answer"))
        assertThat(
            viewModel.constraintValidationResult.getOrAwaitValue(scheduler).value,
            equalTo(failedValidationResult)
        )
    }
}
