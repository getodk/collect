package org.odk.collect.android.widgets;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.FakeLifecycleOwner;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.widgets.viewmodels.RangePickerViewModel;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class RangePickerIntegerWidgetTest {

    private TestScreenContextActivity widgetActivity;
    private RangeQuestion rangeQuestion;
    private RangePickerViewModel rangePickerViewModel;
    private FakeLifecycleOwner fakeLifecycleOwner;

    @Before
    public void setup() {
        widgetActivity = widgetTestActivity();
        rangeQuestion = mock(RangeQuestion.class);
        fakeLifecycleOwner = new FakeLifecycleOwner();

        rangePickerViewModel = new ViewModelProvider(widgetActivity).get(RangePickerViewModel.class);

        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.ONE);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ONE);
    }

    @Test
    public void whenRangeEndIsGreaterThanRangeStart_widgetsSetsCorrectValuesForNumberPicker() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        int index = 0;

        for (int i = 10; i >= 1; i--, index++) {
            assertEquals(widget.displayedValuesForNumberPicker[index], String.valueOf(i));
        }
    }

    @Test
    public void whenRangeStartIsGreaterThanRangeEnd_widgetsSetsCorrectValuesForNumberPicker() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.ONE);
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        int index = 0;

        for (int i = 1; i <= 10; i++, index++) {
            assertEquals(widget.displayedValuesForNumberPicker[index], String.valueOf(i));
        }
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));

        assertNull(widget.getAnswer());
        assertEquals(widget.binding.widgetButton.getText(), widget.getContext().getString(R.string.select_value));
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("4")));

        assertEquals(widget.getAnswer().getValue(), 4);
        assertEquals(widget.binding.widgetButton.getText(), widget.getContext().getString(R.string.edit_value));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
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
    public void clickingWidgetForLong_callsLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);

        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setOnLongClickListener(listener);
        widget.binding.widgetButton.performLongClick();
        widget.binding.widgetAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.widgetButton);
        verify(listener).onLongClick(widget.binding.widgetAnswerText);
    }

    @Test
    public void updatingValueInRangePickerViewModel_updatesAnswer() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        rangePickerViewModel.setNumberPickerValue(5);

        rangePickerViewModel.getNumberPickerValue().observe(fakeLifecycleOwner, integer -> {
            assertEquals(widget.binding.widgetAnswerText.getText(), "5");
            assertEquals(widget.binding.widgetButton.getText(), widget.getContext().getString(R.string.edit_value));
        });
    }

    @Test
    public void updatingValueInRangePickerViewModel_callsValueChangeListener() {
        RangePickerIntegerWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        rangePickerViewModel.setNumberPickerValue(5);

        rangePickerViewModel.getNumberPickerValue().observe(fakeLifecycleOwner, integer -> {
            verify(valueChangedListener).widgetValueChanged(widget);
        });
    }

    private RangePickerIntegerWidget createWidget(FormEntryPrompt prompt) {
        return new RangePickerIntegerWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"), fakeLifecycleOwner);
    }
}
