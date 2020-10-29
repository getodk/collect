package org.odk.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.FormIndex;
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
import org.odk.collect.android.widgets.interfaces.RangeWidgetDataRequester;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class RangePickerIntegerWidgetTest {
    private final TestScreenContextActivity widgetActivity = widgetTestActivity();
    private final RangeWidgetDataRequester rangeWidgetDataRequester = mock(RangeWidgetDataRequester.class);
    private final RangeQuestion rangeQuestion = mock(RangeQuestion.class);

    @Before
    public void setup() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.ONE);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.valueOf(3));
    }

    @Test
    public void whenWidgetIsReadOnly_widgetButtonIsNotDisplayed() {
        assertEquals(createWidget(promptWithReadOnlyAndQuestionDef(rangeQuestion)).binding
                .widgetButton.getVisibility(), View.GONE);
    }

    @Test
    public void whenWidgetIsInvalid_widgetButtonIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.valueOf(4));
        assertFalse(createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null)).binding.widgetButton.isEnabled());
    }

    @Test
    public void whenRangeStepIsZero_widgetButtonIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ZERO);
        assertFalse(createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null)).binding.widgetButton.isEnabled());
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertNull(createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null)).getAnswer());
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_correctValuesAreDisplayed() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertEquals(widget.binding.widgetButton.getText(), widget.getContext().getString(R.string.select_value));
        assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_value_selected));
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        assertEquals(widget.getAnswer().getValue(), 4);
    }

    @Test
    public void whenPromptHasAnswer_correctValuesAreDisplayed() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        assertEquals(widget.binding.widgetButton.getText(), widget.getContext().getString(R.string.edit_value));
        assertEquals(widget.binding.widgetAnswerText.getText(), "4");
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        widget.clearAnswer();
        assertNull(widget.getAnswer());
    }

    @Test
    public void clearAnswer_clearsWidgetDisplayedAnswer() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));
        widget.clearAnswer();
        assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_value_selected));
        assertEquals(widget.binding.widgetButton.getText(), widget.getContext().getString(R.string.select_value));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void setData_updatesAnswer() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setData(2);
        assertEquals(widget.getAnswer().getDisplayText(), "4");
    }

    @Test
    public void setData_updatesDisplayedAnswer() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setData(2);
        assertEquals(widget.binding.widgetButton.getText(), widget.getContext().getString(R.string.edit_value));
        assertEquals(widget.getAnswer().getDisplayText(), "4");
    }

    @Test
    public void setData_callsValueChangeListener() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setData(5);
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingWidgetButton_requestsRangePickerValue_whenPromptDoesNotHaveAnswer() {
        FormIndex formIndex = mock(FormIndex.class);
        FormEntryPrompt prompt = promptWithQuestionDefAndAnswer(rangeQuestion,  null);
        when(prompt.getIndex()).thenReturn(formIndex);

        RangePickerIntegerWidget widget = createWidget(prompt);
        widget.binding.widgetButton.performClick();

        verify(rangeWidgetDataRequester).requestRangePickerValue(formIndex,
                new String[]{"10", "7", "4", "1"}, 0);
    }

    @Test
    public void clickingWidgetButton_requestsRangePickerValue_whenPromptHasAnswer() {
        FormIndex formIndex = mock(FormIndex.class);
        FormEntryPrompt prompt = promptWithQuestionDefAndAnswer(rangeQuestion,  new StringData("4"));
        when(prompt.getIndex()).thenReturn(formIndex);

        RangePickerIntegerWidget widget = createWidget(prompt);
        widget.binding.widgetButton.performClick();

        verify(rangeWidgetDataRequester).requestRangePickerValue(formIndex,
                new String[]{"10", "7", "4", "1"}, 1);
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
        return new RangePickerIntegerWidget(widgetActivity, new QuestionDetails(prompt,
                "formAnalyticsID"), rangeWidgetDataRequester);
    }
}
