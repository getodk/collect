package org.odk.collect.android.widgets

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.data.GeoPointData
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.utilities.Appearances
import org.odk.collect.android.widgets.interfaces.GeoDataRequester
import org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAppearance
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndAnswer
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetDependencies
import org.odk.collect.android.widgets.utilities.GeoWidgetUtils
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry
import org.odk.collect.androidtest.onNodeWithClickLabel
import org.odk.collect.strings.R.string

@RunWith(AndroidJUnit4::class)
class GeoPointWidgetTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val activity = CollectHelpers.buildThemedActivity(WidgetTestActivity::class.java).setup().get()

    private val answer = GeoPointData(getRandomDoubleArray())

    private val geoDataRequester = mock<GeoDataRequester>()
    private val waitingForDataRegistry = mock<WaitingForDataRegistry>()

    @Test
    fun whenPromptIsReadOnlyAndHasNoAnswer_geoPointButtonIsHidden() {
        createWidget(promptWithReadOnly())
        composeRule.onNodeWithClickLabel(string.get_point).assertDoesNotExist()
    }

    @Test
    fun whenPromptIsNotReadOnlyAndHasNoAnswer_geoPointButtonIsShown() {
        createWidget(promptWithAnswer(null))
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun whenPromptIsReadOnlyAndHasAnswer_geoPointButtonIsShown() {
        createWidget(promptWithReadOnlyAndAnswer(answer))
        composeRule.onNodeWithClickLabel(string.view_point).assertIsDisplayed()
    }

    @Test
    fun withMapsAppearance_whenPromptIsReadOnlyAndHasNoAnswer_geoButtonIsNotDisplayed() {
        val prompt = MockFormEntryPromptBuilder()
            .withAppearance(Appearances.MAPS)
            .withReadOnly(true)
            .withAnswer(null)
            .build()
        createWidget(prompt)
        composeRule.onNodeWithClickLabel(string.get_point).assertDoesNotExist()
    }

    @Test
    fun withMapsAppearance_whenPromptIsReadOnlyAndHasAnswer_viewGeoPointButtonIsShown() {
        val prompt = MockFormEntryPromptBuilder()
            .withAppearance(Appearances.MAPS)
            .withReadOnly(true)
            .withAnswer(answer)
            .build()
        createWidget(prompt)
        composeRule.onNodeWithClickLabel(string.view_point).assertIsDisplayed()
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
        createWidget(promptWithAnswer(StringData("blah")))
        composeRule.onNodeWithText("blah").assertDoesNotExist()
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun answerTextViewShouldShowCorrectAnswer() {
        createWidget(promptWithAnswer(answer))
        composeRule.onNodeWithText(answerToDisplay(answer)).assertIsDisplayed()
    }

    @Test
    fun whenPromptDoesNotHaveAnswer_buttonShowsCorrectText() {
        createWidget(promptWithAnswer(null))
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun whenPromptHasAnswer_buttonShowsCorrectText() {
        createWidget(promptWithAnswer(answer))
        composeRule.onNodeWithClickLabel(string.change_point).assertIsDisplayed()
    }

    @Test
    fun withMapsAppearance_whenPromptDoesNotHaveAnswer_buttonShowsCorrectText() {
        createWidget(promptWithAppearance(Appearances.MAPS))
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun withMapsAppearance_whenPromptHasAnswer_buttonShowsCorrectText() {
        val prompt = MockFormEntryPromptBuilder()
            .withAppearance(Appearances.MAPS)
            .withAnswer(answer)
            .build()
        createWidget(prompt)
        composeRule.onNodeWithClickLabel(string.view_or_change_point).assertIsDisplayed()
    }

    @Test
    fun withPlacementMapAppearance_whenPromptHasAnswer_buttonShowsCorrectText() {
        val prompt = MockFormEntryPromptBuilder()
            .withAppearance(Appearances.PLACEMENT_MAP)
            .withAnswer(answer)
            .build()
        createWidget(prompt)
        composeRule.onNodeWithClickLabel(string.view_or_change_point).assertIsDisplayed()
    }

    @Test
    fun clearAnswer_clearsWidgetAnswer() {
        val widget = createWidget(promptWithAnswer(answer))
        widget.clearAnswer()

        assertThat(widget.answer, equalTo(null))
        composeRule.onNodeWithText(answerToDisplay(answer)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun clearAnswer_callsValueChangeListeners() {
        val widget = createWidget(promptWithAnswer(null))
        val valueChangedListener = mockValueChangedListener(widget)
        widget.clearAnswer()

        verify(valueChangedListener).widgetValueChanged(widget)
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
        composeRule.onNodeWithText(answerToDisplay(answer)).assertIsDisplayed()
    }

    @Test
    fun setDataWithInvalidValue_doesNotUpdateWidgetDisplayedAnswer() {
        val widget = createWidget(promptWithAnswer(null))
        widget.setData("blah")
        composeRule.onNodeWithText("blah").assertDoesNotExist()
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun setData_whenDataIsNull_updatesButtonLabel() {
        val widget = createWidget(promptWithAnswer(answer))
        widget.setData("")
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun setData_whenDataIsNotNull_updatesButtonLabel() {
        val widget = createWidget(promptWithAnswer(null))
        widget.setData(answer.displayText)
        composeRule.onNodeWithClickLabel(string.change_point).assertIsDisplayed()
    }

    @Test
    fun withMapsAppearance_setData_whenDataIsNull_updatesButtonLabel() {
        val prompt = MockFormEntryPromptBuilder()
            .withAppearance(Appearances.MAPS)
            .withAnswer(answer)
            .build()
        val widget = createWidget(prompt)
        widget.setData("")
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun withMapsAppearance_setData_whenDataIsNotNull_updatesButtonLabel() {
        val widget = createWidget(promptWithAppearance(Appearances.MAPS))
        widget.setData(answer.displayText)
        composeRule.onNodeWithClickLabel(string.view_or_change_point).assertIsDisplayed()
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
        createWidget(prompt)
        composeRule.onNodeWithClickLabel(string.change_point).performClick()

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry)
    }

    @Test
    fun whenPromptIsReadOnlyAndHasAnswer_buttonClick_requestsGeoPoint() {
        val prompt = promptWithReadOnlyAndAnswer(answer)
        createWidget(prompt)
        composeRule.onNodeWithClickLabel(string.view_point).performClick()

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry)
    }

    @Test
    fun buttonClick_requestsGeoPoint_whenAnswerIsCleared() {
        val prompt = promptWithAnswer(answer)
        val widget = createWidget(prompt)
        widget.clearAnswer()
        composeRule.onNodeWithClickLabel(string.get_point).performClick()

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry)
    }

    @Test
    fun buttonClick_requestsGeoPoint_whenAnswerIsUpdated() {
        val prompt = promptWithAnswer(null)
        val widget = createWidget(prompt)
        widget.setData(answer)
        composeRule.onNodeWithClickLabel(string.change_point).performClick()

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry)
    }

    private fun answerToDisplay(answer: GeoPointData) =
        GeoWidgetUtils.getGeoPointAnswerToDisplay(activity, answer.displayText)

    private fun createWidget(prompt: FormEntryPrompt): GeoPointWidget {
        return GeoPointWidget(
            activity,
            QuestionDetails(prompt),
            waitingForDataRegistry,
            geoDataRequester,
            widgetDependencies()
        ).also {
            activity.setContentView(it)
        }
    }
}
