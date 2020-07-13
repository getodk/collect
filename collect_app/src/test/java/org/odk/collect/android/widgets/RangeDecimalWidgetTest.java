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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

/**
 * @author James Knight
 */

@RunWith(RobolectricTestRunner.class)
public class RangeDecimalWidgetTest {

    private RangeQuestion rangeQuestion;

    @Before
    public void setup() {
        rangeQuestion = mock(RangeQuestion.class);

        when(rangeQuestion.getRangeStart()).thenReturn(new BigDecimal("1.5"));
        when(rangeQuestion.getRangeEnd()).thenReturn(new BigDecimal("5.5"));
        when(rangeQuestion.getRangeStep()).thenReturn(new BigDecimal("0.5"));
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
    public void whenPromptHasAnswer_sliderShouldShowCorrectAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.slider.getValue(), equalTo(2.5F));
        assertThat(widget.currentValue.getText(), equalTo("2.5"));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);

        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void changingSliderValue_updatesAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.slider.setValue(2.5F);

        assertThat(widget.getAnswer().getValue(), equalTo(2.5));
        assertThat(widget.currentValue.getText(), equalTo("2.5"));
    }

    @Test
    public void changingSliderValue_callsValueChangeListener() {
        RangeDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.slider.setValue(2.5F);

        verify(valueChangedListener).widgetValueChanged(widget);
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
