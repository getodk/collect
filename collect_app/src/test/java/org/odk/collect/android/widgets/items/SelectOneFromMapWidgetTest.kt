package org.odk.collect.android.widgets.items

import androidx.lifecycle.ViewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.javarosa.core.model.SelectChoice
import org.javarosa.core.model.data.SelectOneData
import org.javarosa.core.model.data.helper.Selection
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.fakes.FakePermissionsProvider
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.testshared.RobolectricHelpers.getFragmentByClass
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class SelectOneFromMapWidgetTest {

    private val activity = Robolectric.setupActivity(WidgetTestActivity::class.java)
    private val formEntryViewModel = mock<FormEntryViewModel>()
    private val permissionsProvider = FakePermissionsProvider().also {
        it.setPermissionGranted(true)
    }

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesFormEntryViewModelFactory(analytics: Analytics?): FormEntryViewModel.Factory {
                return object : FormEntryViewModel.Factory(System::currentTimeMillis) {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return formEntryViewModel as T
                    }
                }
            }

            override fun providesPermissionsProvider(permissionsChecker: PermissionsChecker): PermissionsProvider =
                permissionsProvider
        })
    }

    @Test
    fun `clicking button opens SelectOneFromMapDialogFragment`() {
        val prompt = promptWithAnswer(null)
        val widget = SelectOneFromMapWidget(activity, QuestionDetails(prompt))
        whenever(formEntryViewModel.getQuestionPrompt(prompt.index)).doReturn(prompt)

        widget.binding.button.performClick()

        val fragment = getFragmentByClass(
            activity.supportFragmentManager,
            SelectOneFromMapDialogFragment::class.java
        )
        assertThat(fragment, notNullValue())
        assertThat(
            fragment?.requireArguments()
                ?.getSerializable(SelectOneFromMapDialogFragment.ARG_FORM_INDEX),
            equalTo(prompt.index)
        )
    }

    @Test
    fun `clicking button when location permissions denined does nothing`() {
        val widget = SelectOneFromMapWidget(activity, QuestionDetails(promptWithAnswer(null)))

        permissionsProvider.setPermissionGranted(false)
        widget.binding.button.performClick()

        val fragment = getFragmentByClass(
            activity.supportFragmentManager,
            SelectOneFromMapDialogFragment::class.java
        )
        assertThat(fragment, nullValue())
    }

    @Test
    fun `shows answer`() {
        val choices = listOf(
            SelectChoice(null, "A", "a", false),
            SelectChoice(null, "B", "b", false),
        )
        val prompt = MockFormEntryPromptBuilder()
            .withSelectChoices(choices)
            .withAnswer(SelectOneData(choices[0].selection()))
            .build()

        val widget = SelectOneFromMapWidget(activity, QuestionDetails(prompt))
        assertThat(widget.binding.answer.text, equalTo("A"))
    }

    @Test
    fun `prompt answer is returned from getAnswer`() {
        val answer = SelectOneData(Selection(101))
        val widget = SelectOneFromMapWidget(activity, QuestionDetails(promptWithAnswer(answer)))
        assertThat(widget.answer, equalTo(answer))
    }

    @Test
    fun `clearAnswer removes answer`() {
        val answer = SelectOneData(Selection(101))
        val widget = SelectOneFromMapWidget(activity, QuestionDetails(promptWithAnswer(answer)))
        widget.clearAnswer()
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    fun `clearAnswer updates shown answer`() {
        val choices = listOf(
            SelectChoice(null, "A", "a", false),
            SelectChoice(null, "B", "b", false),
        )
        val prompt = MockFormEntryPromptBuilder()
            .withSelectChoices(choices)
            .withAnswer(SelectOneData(choices[0].selection()))
            .build()

        val widget = SelectOneFromMapWidget(activity, QuestionDetails(prompt))

        widget.clearAnswer()
        assertThat(widget.binding.answer.text, equalTo(""))
    }

    @Test
    fun `setData sets answer`() {
        val widget = SelectOneFromMapWidget(activity, QuestionDetails(promptWithAnswer(null)))

        val answer = SelectOneData(Selection(101))
        widget.setData(answer)
        assertThat(widget.answer, equalTo(answer))
    }

    @Test
    fun `setData updates shown answer`() {
        val choices = listOf(
            SelectChoice(null, "A", "a", false),
            SelectChoice(null, "B", "b", false),
        )
        val prompt = MockFormEntryPromptBuilder()
            .withSelectChoices(choices)
            .build()
        val widget = SelectOneFromMapWidget(activity, QuestionDetails(prompt))

        widget.setData(SelectOneData(choices[1].selection()))
        assertThat(widget.binding.answer.text, equalTo("B"))
    }
}
