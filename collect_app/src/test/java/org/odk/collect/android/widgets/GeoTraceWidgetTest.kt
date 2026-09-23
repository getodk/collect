package org.odk.collect.android.widgets

import android.app.Application
import android.content.ComponentName
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.Constants
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.support.WidgetTestActivity
import org.odk.collect.android.widgets.interfaces.GeoDataRequester
import org.odk.collect.android.widgets.support.GeoWidgetHelpers.stringFromDoubleList
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndAnswer
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetDependencies
import org.odk.collect.androidtest.onNodeWithClickLabel
import org.odk.collect.strings.R.string
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class GeoTraceWidgetTest {
    init {
        shadowOf(getApplicationContext<Application>().packageManager)
            .addActivityIfNotPresent(ComponentName(getApplicationContext(), WidgetTestActivity::class.java))
    }

    @get:Rule
    val composeRule = createAndroidComposeRule<WidgetTestActivity>()

    private val answer = stringFromDoubleList()

    private val geoDataRequester = mock<GeoDataRequester>()

    @Test
    fun `#getAnswer returns null when there is no answer`() {
        val widget = createWidget(promptWithAnswer(null))
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    fun `#getAnswer returns the answer when there is one`() {
        val widget = createWidget(promptWithAnswer(StringData(answer)))
        assertThat(widget.answer!!.displayText, equalTo(answer))
    }

    @Test
    fun `the answer is not displayed when there is no answer`() {
        createWidget(promptWithAnswer(null))
        composeRule.onNodeWithText(answer).assertDoesNotExist()
    }

    @Test
    fun `the answer is displayed when there is one`() {
        createWidget(promptWithAnswer(StringData(answer)))
        composeRule.onNodeWithText(answer).assertIsDisplayed()
    }

    @Test
    fun `the button is hidden when the question is read-only and there is no answer`() {
        createWidget(promptWithReadOnly())
        composeRule.onNodeWithClickLabel(string.view_line).assertDoesNotExist()
    }

    @Test
    fun `the button has correct text when the question is read-only and there is an answer`() {
        createWidget(promptWithReadOnlyAndAnswer(StringData(answer)))
        composeRule.onNodeWithClickLabel(string.view_line).assertIsDisplayed()
    }

    @Test
    fun `the button has correct text when the question is not read-only and there is no answer`() {
        createWidget(promptWithAnswer(null))
        composeRule.onNodeWithClickLabel(string.get_line).assertIsDisplayed()
    }

    @Test
    fun `the button has correct text when the question is not read-only and there is an answer`() {
        createWidget(promptWithAnswer(StringData(answer)))
        composeRule.onNodeWithClickLabel(string.view_or_change_line).assertIsDisplayed()
    }

    @Test
    fun `clicking the button requests a geotrace`() {
        val prompt = promptWithAnswer(null)
        createWidget(prompt)

        composeRule.onNodeWithClickLabel(string.get_line).performClick()

        verify(geoDataRequester).requestGeoPoly(prompt)
    }

    @Test
    fun `clicking the button requests a geotrace when there is an answer`() {
        val prompt = promptWithAnswer(StringData(answer))
        createWidget(prompt)

        composeRule.onNodeWithClickLabel(string.view_or_change_line).performClick()

        verify(geoDataRequester).requestGeoPoly(prompt)
    }

    private fun createWidget(prompt: FormEntryPrompt) = GeoTraceWidget(
        composeRule.activity,
        QuestionDetails(asGeoTraceQuestion(prompt)),
        geoDataRequester,
        widgetDependencies()
    ).also {
        composeRule.activity.setContentView(it)
    }

    private fun asGeoTraceQuestion(prompt: FormEntryPrompt) = MockFormEntryPromptBuilder(prompt)
        .withControlType(Constants.CONTROL_INPUT)
        .withDataType(Constants.DATATYPE_GEOTRACE)
        .build()
}
