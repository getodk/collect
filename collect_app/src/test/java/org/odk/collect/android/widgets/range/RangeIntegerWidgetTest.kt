package org.odk.collect.android.widgets.range

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.javarosa.core.model.RangeQuestion
import org.javarosa.core.model.data.IntegerData
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.base.QuestionWidgetTest
import org.odk.collect.android.widgets.support.QuestionWidgetHelpers
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class RangeIntegerWidgetTest : QuestionWidgetTest<RangeIntegerWidget, IntegerData>() {
    private var rangeQuestion = mock<RangeQuestion>().apply {
        whenever(rangeStart).thenReturn(BigDecimal.ONE)
        whenever(rangeEnd).thenReturn(BigDecimal.TEN)
        whenever(rangeStep).thenReturn(BigDecimal.ONE)
    }

    override fun createWidget(): RangeIntegerWidget {
        whenever(formEntryPrompt.question).thenReturn(rangeQuestion)

        return RangeIntegerWidget(
            activity,
            QuestionDetails(formEntryPrompt),
            QuestionWidgetHelpers.widgetDependencies()
        )
    }

    override fun getNextAnswer() = IntegerData(5)

    //
    //    @Test
    //    public void clearAnswer_hidesSliderThumb() {
    //        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2")));
    //        widget.clearAnswer();
    //        assertThat(widget.slider.getThumbRadius(), equalTo(0));
    //    }
    //    @Test
    //    public void changingSliderValue_showsSliderThumb() {
    //        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        SliderExtKt.clickOnMinValue(widget.slider);
    //        assertThat(widget.slider.getThumbRadius(), not(0));
    //    }
    //
    //    @Test
    //    public void changingSliderValue_whenRangeStartIsSmallerThanRangeEnd_updatesAnswer() {
    //        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        SliderExtKt.clickOnMaxValue(widget.slider);
    //        assertThat(widget.currentValue.getText(), equalTo("10"));
    //    }
    //
    //    @Test
    //    public void changingSliderValue_whenRangeStartIsGreaterThanRangeEnd_updatesAnswer() {
    //        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.TEN);
    //        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.ONE);
    //
    //        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        SliderExtKt.clickOnMaxValue(widget.slider);
    //
    //        assertThat(widget.currentValue.getText(), equalTo("1"));
    //    }
    //
    //    @Test
    //    public void changingSliderValue_callsValueChangeListener() {
    //        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
    //        SliderExtKt.clickOnMaxValue(widget.slider);
    //
    //        verify(valueChangedListener).widgetValueChanged(widget);
    //    }
    //
    //    @Test
    //    public void changingSliderValueProgramatically_doesNotUpdateAnswer() {
    //        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        widget.slider.setValue(4);
    //        assertThat(widget.currentValue.getText(), equalTo(""));
    //    }
    //
    //    @Test
    //    public void changingSliderValueProgramatically_doesNotCallValueChangeListener() {
    //        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
    //        widget.slider.setValue(4);
    //
    //        verify(valueChangedListener, never()).widgetValueChanged(widget);
    //    }
    //
    //    @Test // https://github.com/getodk/collect/issues/5530
    //    public void everyTriggerWidgetShouldHaveCheckboxWithUniqueID() {
    //        RangeIntegerWidget widget1 = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //        RangeIntegerWidget widget2 = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //
    //        assertThat(widget1.slider.getId(), not(equalTo(widget2.slider.getId())));
    //    }
    //
    //    @Test
    //    public void changingSliderValueToTheMinOneWhenSliderHasNoValue_setsTheValueCorrectly() {
    //        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //
    //        SliderExtKt.clickOnMinValue(widget.slider);
    //
    //        assertThat(widget.currentValue.getText(), equalTo("1"));
    //    }
    //
    //    @Test
    //    public void changingSliderValueToAnyOtherThanTheMinOne_setsTheValueCorrectly() {
    //        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
    //
    //        SliderExtKt.clickOnMaxValue(widget.slider);
    //
    //        assertThat(widget.currentValue.getText(), equalTo("10"));
    //    }

    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
    }
}
