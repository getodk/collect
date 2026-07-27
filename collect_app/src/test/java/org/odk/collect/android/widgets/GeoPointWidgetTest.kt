package org.odk.collect.android.widgets

import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.data.GeoPointData
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.interfaces.GeoDataRequester
import org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAppearance
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndAnswer
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetDependencies
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry

@RunWith(AndroidJUnit4::class)
class GeoPointWidgetTest {
    private val answer = GeoPointData(getRandomDoubleArray())

    private val geoDataRequester = mock<GeoDataRequester>()
    private val waitingForDataRegistry = mock<WaitingForDataRegistry>()

    @Test
    fun whenPromptIsReadOnlyAndHasNoAnswer_geoPointButtonIsHidden() {
        val widget = createWidget(promptWithReadOnly())
        assertThat(widget.binding.simpleButton.visibility, equalTo(View.GONE))
    }

    @Test
    fun whenPromptIsNotReadOnlyAndHasNoAnswer_geoPointButtonIsShown() {
        val widget = createWidget(promptWithAnswer(null))
        assertThat(widget.binding.simpleButton.visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun whenPromptIsReadOnlyAndHasAnswer_geoPointButtonIsShown() {
        val widget = createWidget(promptWithReadOnlyAndAnswer(answer))
        assertThat(widget.binding.simpleButton.visibility, equalTo(View.VISIBLE))
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.view_point))
        )
    }

    @Test
    fun withMapsAppearance_whenPromptIsReadOnlyAndHasNoAnswer_geoButtonIsNotDisplayed() {
        val prompt = MockFormEntryPromptBuilder()
            .withAppearance(Appearances.MAPS)
            .withReadOnly(true)
            .withAnswer(null)
            .build()
        val widget = createWidget(prompt)
        assertThat(widget.binding.simpleButton.visibility, equalTo(View.GONE))
    }

    @Test
    fun withMapsAppearance_whenPromptIsReadOnlyAndHasAnswer_viewGeoPointButtonIsShown() {
        val prompt = MockFormEntryPromptBuilder()
            .withAppearance(Appearances.MAPS)
            .withReadOnly(true)
            .withAnswer(answer)
            .build()
        val widget = createWidget(prompt)
        assertThat(widget.binding.simpleButton.visibility, equalTo(View.VISIBLE))
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.view_point))
        )
    }

    @Test
    fun getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        val widget = createWidget(promptWithAnswer(null))
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    fun getAnswer_whenPromptHasAnswer_returnsAnswer() {
        val widget = createWidget(promptWithAnswer(answer))
        assertThat(widget.answer!!.displayText, equalTo(answer.displayText))
    }

    @Test
    fun getAnswer_whenPromptHasInvalidAnswer_returnsNull() {
        val widget = createWidget(promptWithAnswer(StringData("blah")))
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    fun creatingWidgetWithInvalidValue_doesNotUpdateWidgetDisplayedAnswer() {
        val widget = createWidget(promptWithAnswer(StringData("blah")))
        assertThat(widget.binding.geoAnswerText.text.toString(), equalTo(""))
        assertThat(widget.binding.geoAnswerText.visibility, equalTo(View.GONE))
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.get_point))
        )
    }

    @Test
    fun answerTextViewShouldShowCorrectAnswer() {
        val widget = createWidget(promptWithAnswer(answer))
        assertThat(
            widget.binding.geoAnswerText.text.toString(),
            equalTo(GeoWidgetUtils.getGeoPointAnswerToDisplay(widget.context, answer.displayText))
        )
        assertThat(widget.binding.geoAnswerText.visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun whenPromptDoesNotHaveAnswer_buttonShowsCorrectText() {
        val widget = createWidget(promptWithAnswer(null))
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.get_point))
        )
    }

    @Test
    fun whenPromptHasAnswer_buttonShowsCorrectText() {
        val widget = createWidget(promptWithAnswer(answer))
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.change_point))
        )
    }

    @Test
    fun withMapsAppearance_whenPromptDoesNotHaveAnswer_buttonShowsCorrectText() {
        val widget = createWidget(promptWithAppearance(Appearances.MAPS))
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.get_point))
        )
    }

    @Test
    fun withMapsAppearance_whenPromptHasAnswer_buttonShowsCorrectText() {
        val prompt = MockFormEntryPromptBuilder()
            .withAppearance(Appearances.MAPS)
            .withAnswer(answer)
            .build()
        val widget = createWidget(prompt)
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.view_or_change_point))
        )
    }

    @Test
    fun withPlacementMapAppearance_whenPromptHasAnswer_buttonShowsCorrectText() {
        val prompt = MockFormEntryPromptBuilder()
            .withAppearance(Appearances.PLACEMENT_MAP)
            .withAnswer(answer)
            .build()
        val widget = createWidget(prompt)
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.view_or_change_point))
        )
    }

    @Test
    fun clearAnswer_clearsWidgetAnswer() {
        val widget = createWidget(promptWithAnswer(answer))
        widget.clearAnswer()

        assertThat(widget.binding.geoAnswerText.text.toString(), equalTo(""))
        assertThat(widget.binding.geoAnswerText.visibility, equalTo(View.GONE))
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.get_point))
        )
    }

    @Test
    fun clearAnswer_callsValueChangeListeners() {
        val widget = createWidget(promptWithAnswer(null))
        val valueChangedListener = mockValueChangedListener(widget)
        widget.clearAnswer()

        verify(valueChangedListener).widgetValueChanged(widget)
    }

    @Test
    fun clickingButtonAndAnswerTextViewForLong_callsLongClickListeners() {
        val listener = mock<View.OnLongClickListener>()
        val widget = createWidget(promptWithAnswer(null))

        widget.setOnLongClickListener(listener)
        widget.binding.simpleButton.performLongClick()
        widget.binding.geoAnswerText.performLongClick()

        verify(listener).onLongClick(widget.binding.simpleButton)
        verify(listener).onLongClick(widget.binding.geoAnswerText)
    }

    @Test
    fun setData_updatesWidgetAnswer() {
        val widget = createWidget(promptWithAnswer(null))
        widget.setData(answer.displayText)
        assertThat(widget.answer!!.displayText, equalTo(answer.displayText))
    }

    @Test
    fun setDataWithInvalidValue_doesNotUpdateWidgetAnswer() {
        val widget = createWidget(promptWithAnswer(null))
        widget.setData("blah")
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    fun setData_updatesWidgetDisplayedAnswer() {
        val widget = createWidget(promptWithAnswer(null))
        widget.setData(answer.displayText)
        assertThat(
            widget.binding.geoAnswerText.text.toString(),
            equalTo(GeoWidgetUtils.getGeoPointAnswerToDisplay(widget.context, answer.displayText))
        )
        assertThat(widget.binding.geoAnswerText.visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun setDataWithInvalidValue_doesNotUpdateWidgetDisplayedAnswer() {
        val widget = createWidget(promptWithAnswer(null))
        widget.setData("blah")
        assertThat(widget.binding.geoAnswerText.text.toString(), equalTo(""))
        assertThat(widget.binding.geoAnswerText.visibility, equalTo(View.GONE))
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.get_point))
        )
    }

    @Test
    fun setData_whenDataIsNull_updatesButtonLabel() {
        val widget = createWidget(promptWithAnswer(answer))
        widget.setData("")
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.get_point))
        )
    }

    @Test
    fun setData_whenDataIsNotNull_updatesButtonLabel() {
        val widget = createWidget(promptWithAnswer(null))
        widget.setData(answer.displayText)
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.change_point))
        )
    }

    @Test
    fun withMapsAppearance_setData_whenDataIsNull_updatesButtonLabel() {
        val prompt = MockFormEntryPromptBuilder()
            .withAppearance(Appearances.MAPS)
            .withAnswer(answer)
            .build()
        val widget = createWidget(prompt)
        widget.setData("")
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.get_point))
        )
    }

    @Test
    fun withMapsAppearance_setData_whenDataIsNotNull_updatesButtonLabel() {
        val widget = createWidget(promptWithAppearance(Appearances.MAPS))
        widget.setData(answer.displayText)
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.view_or_change_point))
        )
    }

    @Test
    fun setData_callsValueChangeListener() {
        val widget = createWidget(promptWithAnswer(null))
        val valueChangedListener = mockValueChangedListener(widget)
        widget.setData(answer.displayText)
        verify(valueChangedListener).widgetValueChanged(widget)
    }

    @Test
    fun buttonClick_requestsGeoPoint() {
        val prompt = promptWithAnswer(answer)
        val widget = createWidget(prompt)
        widget.binding.simpleButton.performClick()

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry)
    }

    @Test
    fun whenPromptIsReadOnlyAndHasAnswer_buttonClick_requestsGeoPoint() {
        val prompt = promptWithReadOnlyAndAnswer(answer)
        val widget = createWidget(prompt)
        widget.binding.simpleButton.performClick()

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry)
    }

    @Test
    fun buttonClick_requestsGeoPoint_whenAnswerIsCleared() {
        val prompt = promptWithAnswer(answer)
        val widget = createWidget(prompt)
        widget.clearAnswer()
        widget.binding.simpleButton.performClick()

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry)
    }

    @Test
    fun buttonClick_requestsGeoPoint_whenAnswerIsUpdated() {
        val prompt = promptWithAnswer(null)
        val widget = createWidget(prompt)
        widget.setData(answer)
        widget.binding.simpleButton.performClick()

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry)
    }

    private fun createWidget(prompt: FormEntryPrompt): GeoPointWidget {
        return GeoPointWidget(
            widgetTestActivity(),
            QuestionDetails(prompt),
            waitingForDataRegistry,
            geoDataRequester,
            widgetDependencies()
        )
    }
}
