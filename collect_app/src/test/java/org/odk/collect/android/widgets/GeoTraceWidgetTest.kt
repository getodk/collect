package org.odk.collect.android.widgets

import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.interfaces.GeoDataRequester
import org.odk.collect.android.widgets.support.GeoWidgetHelpers.stringFromDoubleList
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndAnswer
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetDependencies
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity

@RunWith(AndroidJUnit4::class)
class GeoTraceWidgetTest {
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
    fun `the answer text is empty when there is no answer`() {
        val widget = createWidget(promptWithAnswer(null))
        assertThat(widget.binding.geoAnswerText.text.toString(), equalTo(""))
    }

    @Test
    fun `the answer text displays the answer when there is one`() {
        val widget = createWidget(promptWithAnswer(StringData(answer)))
        assertThat(widget.binding.geoAnswerText.text.toString(), equalTo(answer))
    }

    @Test
    fun `the button is hidden when the question is read-only and there is no answer`() {
        val widget = createWidget(promptWithReadOnly())
        assertThat(widget.binding.simpleButton.visibility, equalTo(View.GONE))
    }

    @Test
    fun `the button has correct text when the question is read-only and there is an answer`() {
        val widget = createWidget(promptWithReadOnlyAndAnswer(StringData(answer)))
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.view_line))
        )
    }

    @Test
    fun `the button has correct text when the question is not read-only and there is no answer`() {
        val widget = createWidget(promptWithAnswer(null))
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.get_line))
        )
    }

    @Test
    fun `the button has correct text when the question is not read-only and there is an answer`() {
        val widget = createWidget(promptWithAnswer(StringData(answer)))
        assertThat(
            widget.binding.simpleButton.text.toString(),
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.view_or_change_line))
        )
    }

    @Test
    fun `long clicking the button and the answer text calls the long click listener`() {
        val listener = mock<View.OnLongClickListener>()
        val widget = createWidget(promptWithAnswer(null))

        widget.setOnLongClickListener(listener)
        widget.binding.simpleButton.performLongClick()
        widget.binding.geoAnswerText.performLongClick()

        verify(listener).onLongClick(widget.binding.simpleButton)
        verify(listener).onLongClick(widget.binding.geoAnswerText)
    }

    @Test
    fun `clicking the button requests a geotrace`() {
        val prompt = promptWithAnswer(null)
        val widget = createWidget(prompt)
        widget.binding.simpleButton.performClick()

        verify(geoDataRequester).requestGeoPoly(prompt)
    }

    @Test
    fun `clicking the button requests a geotrace after the answer has been cleared`() {
        val prompt = promptWithAnswer(StringData(answer))
        val widget = createWidget(prompt)
        widget.clearAnswer()
        widget.binding.simpleButton.performClick()

        verify(geoDataRequester).requestGeoPoly(prompt)
    }

    private fun createWidget(prompt: FormEntryPrompt) = GeoTraceWidget(
        widgetTestActivity(),
        QuestionDetails(prompt),
        geoDataRequester,
        widgetDependencies()
    )
}
