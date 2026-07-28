package org.odk.collect.android.widgets

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.GeoPointData
import org.javarosa.core.model.data.IAnswerData
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
        createWidget(geoPointPrompt(readOnly = true))
        composeRule.onNodeWithClickLabel(string.get_point).assertDoesNotExist()
    }

    @Test
    fun whenPromptIsNotReadOnlyAndHasNoAnswer_geoPointButtonIsShown() {
        createWidget(geoPointPrompt())
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun whenPromptIsReadOnlyAndHasAnswer_geoPointButtonIsShown() {
        createWidget(geoPointPrompt(answer = answer, readOnly = true))
        composeRule.onNodeWithClickLabel(string.view_point).assertIsDisplayed()
    }

    @Test
    fun withMapsAppearance_whenPromptIsReadOnlyAndHasNoAnswer_geoButtonIsNotDisplayed() {
        val prompt = geoPointPrompt(readOnly = true, appearance = Appearances.MAPS)
        createWidget(prompt)
        composeRule.onNodeWithClickLabel(string.get_point).assertDoesNotExist()
    }

    @Test
    fun withMapsAppearance_whenPromptIsReadOnlyAndHasAnswer_viewGeoPointButtonIsShown() {
        val prompt = geoPointPrompt(answer = answer, readOnly = true, appearance = Appearances.MAPS)
        createWidget(prompt)
        composeRule.onNodeWithClickLabel(string.view_point).assertIsDisplayed()
    }

    @Test
    fun getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        val widget = createWidget(geoPointPrompt())
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    fun getAnswer_whenPromptHasAnswer_returnsAnswer() {
        val widget = createWidget(geoPointPrompt(answer = answer))
        assertThat(widget.answer!!.displayText, equalTo(answer.displayText))
    }

    @Test
    fun getAnswer_whenPromptHasInvalidAnswer_returnsNull() {
        val widget = createWidget(geoPointPrompt(answer = StringData("blah")))
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    fun creatingWidgetWithInvalidValue_doesNotUpdateWidgetDisplayedAnswer() {
        createWidget(geoPointPrompt(answer = StringData("blah")))
        composeRule.onNodeWithText("blah").assertDoesNotExist()
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun answerTextViewShouldShowCorrectAnswer() {
        createWidget(geoPointPrompt(answer = answer))
        composeRule.onNodeWithText(answerToDisplay(answer)).assertIsDisplayed()
    }

    @Test
    fun whenPromptDoesNotHaveAnswer_buttonShowsCorrectText() {
        createWidget(geoPointPrompt())
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun whenPromptHasAnswer_buttonShowsCorrectText() {
        createWidget(geoPointPrompt(answer = answer))
        composeRule.onNodeWithClickLabel(string.change_point).assertIsDisplayed()
    }

    @Test
    fun withMapsAppearance_whenPromptDoesNotHaveAnswer_buttonShowsCorrectText() {
        createWidget(geoPointPrompt(appearance = Appearances.MAPS))
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun withMapsAppearance_whenPromptHasAnswer_buttonShowsCorrectText() {
        val prompt = geoPointPrompt(answer = answer, appearance = Appearances.MAPS)
        createWidget(prompt)
        composeRule.onNodeWithClickLabel(string.view_or_change_point).assertIsDisplayed()
    }

    @Test
    fun withPlacementMapAppearance_whenPromptHasAnswer_buttonShowsCorrectText() {
        val prompt = geoPointPrompt(answer = answer, appearance = Appearances.PLACEMENT_MAP)
        createWidget(prompt)
        composeRule.onNodeWithClickLabel(string.view_or_change_point).assertIsDisplayed()
    }

    @Test
    fun clearAnswer_clearsWidgetAnswer() {
        val widget = createWidget(geoPointPrompt(answer = answer))
        widget.clearAnswer()

        assertThat(widget.answer, equalTo(null))
        composeRule.onNodeWithText(answerToDisplay(answer)).assertDoesNotExist()
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun clearAnswer_callsValueChangeListeners() {
        val widget = createWidget(geoPointPrompt())
        val valueChangedListener = mockValueChangedListener(widget)
        widget.clearAnswer()

        verify(valueChangedListener).widgetValueChanged(widget)
    }

    @Test
    fun setData_updatesWidgetAnswer() {
        val widget = createWidget(geoPointPrompt())
        widget.setData(answer.displayText)
        assertThat(widget.answer!!.displayText, equalTo(answer.displayText))
    }

    @Test
    fun setDataWithInvalidValue_doesNotUpdateWidgetAnswer() {
        val widget = createWidget(geoPointPrompt())
        widget.setData("blah")
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    fun setData_updatesWidgetDisplayedAnswer() {
        val widget = createWidget(geoPointPrompt())
        widget.setData(answer.displayText)
        composeRule.onNodeWithText(answerToDisplay(answer)).assertIsDisplayed()
    }

    @Test
    fun setDataWithInvalidValue_doesNotUpdateWidgetDisplayedAnswer() {
        val widget = createWidget(geoPointPrompt())
        widget.setData("blah")
        composeRule.onNodeWithText("blah").assertDoesNotExist()
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun setData_whenDataIsNull_updatesButtonLabel() {
        val widget = createWidget(geoPointPrompt(answer = answer))
        widget.setData("")
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun setData_whenDataIsNotNull_updatesButtonLabel() {
        val widget = createWidget(geoPointPrompt())
        widget.setData(answer.displayText)
        composeRule.onNodeWithClickLabel(string.change_point).assertIsDisplayed()
    }

    @Test
    fun withMapsAppearance_setData_whenDataIsNull_updatesButtonLabel() {
        val prompt = geoPointPrompt(answer = answer, appearance = Appearances.MAPS)
        val widget = createWidget(prompt)
        widget.setData("")
        composeRule.onNodeWithClickLabel(string.get_point).assertIsDisplayed()
    }

    @Test
    fun withMapsAppearance_setData_whenDataIsNotNull_updatesButtonLabel() {
        val widget = createWidget(geoPointPrompt(appearance = Appearances.MAPS))
        widget.setData(answer.displayText)
        composeRule.onNodeWithClickLabel(string.view_or_change_point).assertIsDisplayed()
    }

    @Test
    fun setData_callsValueChangeListener() {
        val widget = createWidget(geoPointPrompt())
        val valueChangedListener = mockValueChangedListener(widget)
        widget.setData(answer.displayText)
        verify(valueChangedListener).widgetValueChanged(widget)
    }

    @Test
    fun buttonClick_requestsGeoPoint() {
        val prompt = geoPointPrompt(answer = answer)
        createWidget(prompt)
        composeRule.onNodeWithClickLabel(string.change_point).performClick()

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry)
    }

    @Test
    fun whenPromptIsReadOnlyAndHasAnswer_buttonClick_requestsGeoPoint() {
        val prompt = geoPointPrompt(answer = answer, readOnly = true)
        createWidget(prompt)
        composeRule.onNodeWithClickLabel(string.view_point).performClick()

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry)
    }

    @Test
    fun buttonClick_requestsGeoPoint_whenAnswerIsCleared() {
        val prompt = geoPointPrompt(answer = answer)
        val widget = createWidget(prompt)
        widget.clearAnswer()
        composeRule.onNodeWithClickLabel(string.get_point).performClick()

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry)
    }

    @Test
    fun buttonClick_requestsGeoPoint_whenAnswerIsUpdated() {
        val prompt = geoPointPrompt()
        val widget = createWidget(prompt)
        widget.setData(answer)
        composeRule.onNodeWithClickLabel(string.change_point).performClick()

        verify(geoDataRequester).requestGeoPoint(prompt, waitingForDataRegistry)
    }

    private fun geoPointPrompt(
        answer: IAnswerData? = null,
        readOnly: Boolean = false,
        appearance: String? = null
    ): FormEntryPrompt {
        return MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_INPUT)
            .withDataType(Constants.DATATYPE_GEOPOINT)
            .withAnswer(answer)
            .withReadOnly(readOnly)
            .withAppearance(appearance)
            .build()
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
