package org.odk.collect.android.widgets;

import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.TestScreenContextActivity;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(AndroidJUnit4.class)
public class RangePickerIntegerWidgetTest {

    private TestScreenContextActivity widgetActivity;
    private RangeQuestion rangeQuestion;

    @Before
    public void setup() {
        widgetActivity = widgetTestActivity();
        rangeQuestion = mock(RangeQuestion.class);

        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.ONE);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ONE);
    }

    @Test
    public void whenRangeEndIsGreaterThanRangeStart_widgetsSetsCorrectValuesForNumberPicker() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        int index = 0;

        for (int i = 10; i >= 1; i--, index++) {
            assertThat(widget.displayedValuesForNumberPicker[index], equalTo(String.valueOf(i)));
        }
    }

    @Test
    public void whenRangeStartIsGreaterThanRangeEnd_widgetsSetsCorrectValuesForNumberPicker() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.ONE);
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        int index = 0;

        for (int i = 1; i <= 10; i++, index++) {
            assertThat(widget.displayedValuesForNumberPicker[index], equalTo(String.valueOf(i)));
        }
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithReadOnlyAndQuestionDef(rangeQuestion)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        assertThat(widget.getAnswer().getValue(), equalTo(4));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(widget.binding.widgetAnswerText.getText(), equalTo(widget.getContext().getString(R.string.no_value_selected)));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);

        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setNumberPickerValue_updatesAnswer() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setNumberPickerValue(4);

        assertThat(widget.getAnswer().getDisplayText(), equalTo("6"));
    }

    @Test
    public void setNumberPickerValue_whenRangeStartIsGreaterThenRangeEnd_updatesAnswer() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.ONE);

        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setNumberPickerValue(4);

        assertThat(widget.getAnswer().getDisplayText(), equalTo("5"));
    }

    @Test
    public void clickingWidgetForLong_callsLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);

        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setOnLongClickListener(listener);
        widget.binding.widgetButton.performLongClick();
        widget.binding.widgetAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.widgetButton);
        verify(listener).onLongClick(widget.binding.widgetAnswerText);
    }

    private RangePickerIntegerWidget createWidget(FormEntryPrompt prompt) {
        return new RangePickerIntegerWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
