package org.odk.collect.android.widgets.range

import android.view.View.OnLongClickListener
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.javarosa.core.model.RangeQuestion
import org.javarosa.core.model.data.DecimalData
import org.javarosa.core.model.data.IntegerData
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetDependencies
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class RangePickerWidgetTest {
    private val integerRangeQuestion = RangeQuestion().also {
        it.rangeStart = BigDecimal(1)
        it.rangeEnd = BigDecimal(10)
        it.rangeStep = BigDecimal(1)
    }

    private val decimalRangeQuestion = RangeQuestion().also {
        it.rangeStart = BigDecimal(1.5)
        it.rangeEnd = BigDecimal(5.5)
        it.rangeStep = BigDecimal(0.5)
    }

    @Test
    fun `answer returns null when prompt does not have answer`() {
        assertThat(
            createWidget(
                QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef(
                    integerRangeQuestion
                ),
                false
            ).answer,
            nullValue()
        )
    }

    @Test
    fun `answer returns answer when prompt has answer`() {
        assertThat(
            createWidget(
                QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(
                    integerRangeQuestion,
                    IntegerData(4)
                ),
                false
            ).answer!!.value,
            equalTo(4)
        )
    }

    @Test
    fun `answer returns decimal answer when decimal is true and prompt has answer`() {
        assertThat(
            createWidget(
                QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(
                    decimalRangeQuestion,
                    DecimalData(4.0)
                ),
                true
            ).answer!!.value,
            equalTo(4.0)
        )
    }

    @Test
    fun `clearAnswer clears widget answer`() {
        val widget = createWidget(
            QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(
                integerRangeQuestion,
                IntegerData(4)
            ),
            false
        )
        widget.clearAnswer()

        assertThat(widget.answer, nullValue())
        assertThat(
            widget.binding.widgetAnswerText.text,
            equalTo(widget.context.getString(org.odk.collect.strings.R.string.no_value_selected))
        )
    }

    @Test
    fun `clearAnswer calls valueChangeListener`() {
        val widget = createWidget(
            QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(integerRangeQuestion, null),
            false
        )
        val valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget)
        widget.clearAnswer()

        verify(valueChangedListener).widgetValueChanged(widget)
    }

    @Test
    fun `clicking widget for long calls longClickListener`() {
        val listener = mock<OnLongClickListener>()
        val widget = createWidget(
            QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(integerRangeQuestion, null),
            false
        )

        widget.setOnLongClickListener(listener)
        widget.binding.widgetButton.performLongClick()
        widget.binding.widgetAnswerText.performLongClick()

        verify(listener).onLongClick(widget.binding.widgetButton)
        verify(listener).onLongClick(widget.binding.widgetAnswerText)
    }

    private fun createWidget(prompt: FormEntryPrompt, decimal: Boolean): RangePickerWidget {
        return RangePickerWidget(
            QuestionWidgetHelpers.widgetTestActivity(),
            QuestionDetails(prompt),
            widgetDependencies(),
            decimal
        )
    }
}
