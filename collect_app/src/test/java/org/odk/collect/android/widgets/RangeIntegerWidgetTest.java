package org.odk.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class RangeIntegerWidgetTest {
    private static final String NO_TICKS_APPEARANCE = "no-ticks";

    private RangeQuestion rangeQuestion;

    @Before
    public void setup() {
        rangeQuestion = mock(RangeQuestion.class);
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.ONE);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ONE);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertNull(createWidget(promptWithReadOnlyAndQuestionDef(rangeQuestion)).getAnswer());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        assertEquals(widget.getAnswer().getValue(), 4);
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_sliderIsSetOnStartingIndex() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertThat(widget.slider.getValue(), equalTo(1.0F));
        assertThat(widget.currentValue.getText(), equalTo(""));
    }

    @Test
    public void whenPromptHasAnswer_sliderShouldShowCorrectAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        assertThat(widget.slider.getValue(), equalTo(4.0F));
        assertThat(widget.currentValue.getText(), equalTo("4"));
    }

    @Test
    public void whenSliderIsDiscrete_widgetShowsCorrectSliderValues() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));

        assertThat(widget.slider.getValueFrom(), equalTo(1.0F));
        assertThat(widget.slider.getValueTo(), equalTo(10.0F));
        assertThat(widget.slider.getStepSize(), equalTo(1.0F));
        assertThat(widget.slider.getValue(), equalTo(4.0F));
    }

    @Test
    public void whenSliderIsContinuous_widgetShowsCorrectSliderValues() {
        when(rangeQuestion.getAppearanceAttr()).thenReturn(NO_TICKS_APPEARANCE);
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));

        assertThat(widget.slider.getValueFrom(), equalTo(1.0F));
        assertThat(widget.slider.getValueTo(), equalTo(10.0F));
        assertThat(widget.slider.getStepSize(), equalTo(0.0F));
        assertThat(widget.slider.getValue(), equalTo(4.0F));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(widget.currentValue.getText(), equalTo(""));
        assertThat(widget.slider.getValue(), equalTo(1.0F));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);

        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void changingSliderValue_whenRangeStartIsSmallerThanRangeEnd_updatesAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.setValue(4.0F);

        assertThat(widget.getAnswer().getValue(), equalTo(4));
        assertThat(widget.currentValue.getText(), equalTo("4"));
    }

    @Test
    public void changingSliderValue_whenRangeStartIsGreaterThanRangeEnd_updatesAnswer() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.ONE);
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.setValue(4.0F);

        assertThat(widget.getAnswer().getValue(), equalTo(7));
        assertThat(widget.currentValue.getText(), equalTo("7"));
    }

    @Test
    public void changingSliderValue_callsValueChangeListener() {
        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.slider.setValue(4.0F);

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingSliderForLong_doesNotCallLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);

        RangeIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setOnLongClickListener(listener);
        widget.slider.performLongClick();

        verify(listener, never()).onLongClick(widget.slider);
    }

    private RangeIntegerWidget createWidget(FormEntryPrompt prompt) {
        return new RangeIntegerWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
