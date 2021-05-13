package org.odk.collect.android.widgets;

import android.view.MotionEvent;
import android.view.View;

import androidx.test.core.view.MotionEventBuilder;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(AndroidJUnit4.class)
public class RangeDecimalWidgetTest {
    private static final String NO_TICKS_APPEARANCE = "no-ticks";

    private RangeQuestion rangeQuestion;
    private MotionEvent motionEvent;

    @Before
    public void setup() {
        rangeQuestion = mock(RangeQuestion.class);
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.valueOf(1.5));
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.valueOf(5.5));
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.valueOf(0.5));

        motionEvent = MotionEventBuilder.newBuilder().build();
        motionEvent.setAction(MotionEvent.ACTION_DOWN);
        motionEvent.setLocation(50, 0);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithReadOnlyAndQuestionDef(rangeQuestion)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.getAnswer().getValue(), equalTo(2.5));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_sliderShowsNoAnswerMarked() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertThat(widget.slider.getValue(), equalTo(1.5F));
        assertThat(widget.slider.getThumbRadius(), equalTo(0));
    }

    @Test
    public void whenPromptHasAnswer_sliderShowsCorrectAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.slider.getValue(), equalTo(2.5F));
        assertThat(widget.slider.getThumbRadius(), not(0));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_widgetShouldShowNullAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertThat(widget.currentValue.getText(), equalTo(""));
    }

    @Test
    public void whenPromptHasAnswer_widgetShouldShowCorrectAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.currentValue.getText(), equalTo("2.5"));
    }

    @Test
    public void whenSliderIsDiscrete_widgetShowsCorrectSlider() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));

        assertThat(widget.slider.getValueFrom(), equalTo(1.5F));
        assertThat(widget.slider.getValueTo(), equalTo(5.5F));
        assertThat(widget.slider.getStepSize(), equalTo(0.5F));
        assertThat(widget.slider.getValue(), equalTo(2.5F));
    }

    @Test
    public void whenSliderIsContinuous_widgetShowsCorrectSlider() {
        when(rangeQuestion.getAppearanceAttr()).thenReturn(NO_TICKS_APPEARANCE);
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));

        assertThat(widget.slider.getValueFrom(), equalTo(1.5F));
        assertThat(widget.slider.getValueTo(), equalTo(5.5F));
        assertThat(widget.slider.getStepSize(), equalTo(0.0F));
        assertThat(widget.slider.getValue(), equalTo(2.5F));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        widget.clearAnswer();
        assertThat(widget.currentValue.getText(), equalTo(""));
    }

    @Test
    public void clearAnswer_hidesSliderThumb() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        widget.clearAnswer();
        assertThat(widget.slider.getThumbRadius(), equalTo(0));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void changingSliderValue_updatesAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.onTouchEvent(motionEvent);
        assertThat(widget.currentValue.getText(), equalTo("5.5"));
    }

    @Test
    public void changingSliderValue_showsSliderThumb() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.onTouchEvent(motionEvent);
        assertThat(widget.slider.getThumbRadius(), not(0));
    }

    @Test
    public void changingSliderValue_whenRangeStartIsGreaterThanRangeEnd_updatesAnswer() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.valueOf(5.5));
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.valueOf(1.5));

        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.onTouchEvent(motionEvent);

        assertThat(widget.currentValue.getText(), equalTo("1.5"));
    }

    @Test
    public void changingSliderValue_callsValueChangeListener() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.slider.onTouchEvent(motionEvent);

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void changingSliderValueProgramatically_doesNotUpdateAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.setValue(2.5F);
        assertThat(widget.currentValue.getText(), equalTo(""));
    }

    @Test
    public void changingSliderValueProgramatically_doesNotCallValueChangeListener() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.slider.setValue(2.5F);
        verify(valueChangedListener, never()).widgetValueChanged(widget);
    }

    @Test
    public void clickingSliderForLong_doesNotCallLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setOnLongClickListener(listener);
        widget.slider.performLongClick();

        verify(listener, never()).onLongClick(widget.slider);
    }

    private RangeDecimalWidget createWidget(FormEntryPrompt prompt) {
        return new RangeDecimalWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
