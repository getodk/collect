package org.odk.collect.android.widgets.range

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.RangeQuestion
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class RangeDecimalWidgetTest {
    private var rangeQuestion = mock<RangeQuestion>().apply {
        whenever(rangeStart).thenReturn(BigDecimal.valueOf(1.5))
        whenever(rangeEnd).thenReturn(BigDecimal.valueOf(5.5))
        whenever(rangeStep).thenReturn(BigDecimal.valueOf(0.5))
    }

    @Test
    fun getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        val widget = createWidget(
            QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef(
                rangeQuestion
            )
        )
        assertThat(widget.answer, equalTo(null))
    }

    @Test
    fun getAnswer_whenPromptHasAnswer_returnsAnswer() {
        val widget = createWidget(
            QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(
                rangeQuestion,
                StringData("2.5")
            )
        )
        assertThat(widget.answer!!.value, equalTo(2.5))
    }

    //
    //    @Test
    //    public void clearAnswer_clearsWidgetAnswer() {
    //        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
    //        widget.clearAnswer();
    //        assertThat(widget.currentValue.getText(), equalTo(""));
    //    }
    //
    //    @Test
    //    public void clearAnswer_hidesSliderThumb() {
    //        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
    //        widget.clearAnswer();
    //        assertThat(widget.slider.getThumbRadius(), equalTo(0));
    //    }
    @Test
    fun clearAnswer_callsValueChangeListener() {
        val widget = createWidget(
            QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(
                rangeQuestion,
                StringData("2.5")
            )
        )
        val valueChangedListener = QuestionWidgetHelpers.mockValueChangedListener(widget)
        widget.clearAnswer()

        verify(valueChangedListener).widgetValueChanged(widget)
    }

    //    @Test
    //    public void changingSliderValue_updatesAnswer() {
    //        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        SliderExtKt.clickOnMaxValue(widget.slider);
    //        assertThat(widget.currentValue.getText(), equalTo("5.5"));
    //    }
    //
    //    @Test
    //    public void changingSliderValue_showsSliderThumb() {
    //        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        SliderExtKt.clickOnMinValue(widget.slider);
    //        assertThat(widget.slider.getThumbRadius(), not(0));
    //    }
    //
    //    @Test
    //    public void changingSliderValue_whenRangeStartIsGreaterThanRangeEnd_updatesAnswer() {
    //        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.valueOf(5.5));
    //        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.valueOf(1.5));
    //
    //        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        SliderExtKt.clickOnMaxValue(widget.slider);
    //
    //        assertThat(widget.currentValue.getText(), equalTo("1.5"));
    //    }
    //
    //    @Test
    //    public void changingSliderValue_callsValueChangeListener() {
    //        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
    //        SliderExtKt.clickOnMaxValue(widget.slider);
    //
    //        verify(valueChangedListener).widgetValueChanged(widget);
    //    }
    //
    //    @Test
    //    public void changingSliderValueProgramatically_doesNotUpdateAnswer() {
    //        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        widget.slider.setValue(2.5F);
    //        assertThat(widget.currentValue.getText(), equalTo(""));
    //    }
    //
    //    @Test
    //    public void changingSliderValueProgramatically_doesNotCallValueChangeListener() {
    //        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
    //        widget.slider.setValue(2.5F);
    //        verify(valueChangedListener, never()).widgetValueChanged(widget);
    //    }
    //
    //    @Test // https://github.com/getodk/collect/issues/5530
    //    public void everyTriggerWidgetShouldHaveCheckboxWithUniqueID() {
    //        RangeDecimalWidget widget1 = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        RangeDecimalWidget widget2 = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //
    //        assertThat(widget1.slider.getId(), not(equalTo(widget2.slider.getId())));
    //    }
    //
    //    @Test
    //    public void changingSliderValueToTheMinOneWhenSliderHasNoValue_setsTheValue() {
    //        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //
    //        SliderExtKt.clickOnMinValue(widget.slider);
    //
    //        assertThat(widget.currentValue.getText(), equalTo("1.5"));
    //    }
    //
    //    @Test
    //    public void changingSliderValueToAnyOtherThanTheMinOne_setsTheValueCorrectly() {
    //        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //
    //        SliderExtKt.clickOnMaxValue(widget.slider);
    //
    //        assertThat(widget.currentValue.getText(), equalTo("5.5"));
    //    }
    private fun createWidget(prompt: FormEntryPrompt?): RangeDecimalWidget {
        val widget = RangeDecimalWidget(
            QuestionWidgetHelpers.widgetTestActivity(),
            QuestionDetails(prompt),
            QuestionWidgetHelpers.widgetDependencies()
        )
        return widget
    }
}
