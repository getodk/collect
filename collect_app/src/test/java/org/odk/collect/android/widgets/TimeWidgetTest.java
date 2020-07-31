package org.odk.collect.android.widgets;

import android.view.View;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.FakeLifecycleOwner;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.widgets.interfaces.DateTimeWidgetListener;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;
import org.odk.collect.android.widgets.viewmodels.DateTimeViewModel;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class TimeWidgetTest {

    private TestScreenContextActivity widgetActivity;
    private LifecycleOwner fakeLifecycleOwner;
    private DateTimeViewModel dateTimeViewModel;
    private DateTimeWidgetListener dateTimeWidgetListener;
    private View.OnLongClickListener onLongClickListener;

    private QuestionDef questionDef;
    private LocalDateTime timeAnswer;

    @Before
    public void setUp() {
        widgetActivity = widgetTestActivity();
        questionDef = mock(QuestionDef.class);
        onLongClickListener = mock(View.OnLongClickListener.class);
        dateTimeWidgetListener = mock(DateTimeWidgetListener.class);

        fakeLifecycleOwner = new FakeLifecycleOwner();
        dateTimeViewModel = new ViewModelProvider(widgetActivity).get(DateTimeViewModel.class);
        timeAnswer = DateTimeWidgetUtils.getSelectedTime(new LocalDateTime().withTime(12, 10, 0, 0), LocalDateTime.now());
    }

    @Test
    public void usingReadOnlyOption_doesNotShowButton() {
        TimeWidget widget = createWidget(promptWithReadOnlyAndQuestionDef(questionDef));
        assertEquals(widget.binding.widgetButton.getVisibility(), View.GONE);
    }

    @Test
    public void whenPromptIsNotReadOnly_buttonShowsCorrectText() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        assertEquals(widget.binding.widgetButton.getText(), widget.getContext().getString(R.string.select_time));
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithQuestionDefAndAnswer(questionDef, null)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsTime() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new TimeData(timeAnswer.toDateTime().toDate())));
        assertEquals(widget.getAnswer().getDisplayText(), new TimeData(timeAnswer.toDateTime().toDate()).getDisplayText());
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_answerTextViewShowsNoTimeSelected() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_time_selected));
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectTime() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new TimeData(timeAnswer.toDateTime().toDate())));
        assertEquals(widget.binding.widgetAnswerText.getText(), DateTimeWidgetUtils.getTimeData(timeAnswer.toDateTime()).getDisplayText());
    }

    @Test
    public void updatingTimeInDateTimeViewModel_doesNotUpdateAnswer_whenWidgetIsNotWaitingForData() {
        FormEntryPrompt prompt = promptWithQuestionDefAndAnswer(questionDef, null);
        TimeWidget widget = createWidget(prompt);

        when(dateTimeWidgetListener.isWidgetWaitingForData(prompt.getIndex())).thenReturn(false);
        dateTimeViewModel.setSelectedTime(12, 10);

        dateTimeViewModel.getSelectedTime().observe(fakeLifecycleOwner, localDateTime -> {
            assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_time_selected));
        });
    }

    @Test
    public void updatingTimeInDateTimeViewModel_updatesAnswer_whenWidgetIsWaitingForData() {
        FormEntryPrompt prompt = promptWithQuestionDefAndAnswer(questionDef, null);
        TimeWidget widget = createWidget(prompt);

        when(dateTimeWidgetListener.isWidgetWaitingForData(prompt.getIndex())).thenReturn(true);
        dateTimeViewModel.setSelectedTime(12, 10);

        dateTimeViewModel.getSelectedDate().observe(fakeLifecycleOwner, localDateTime -> {
            assertEquals(widget.binding.widgetAnswerText.getText(), DateTimeWidgetUtils.getTimeData(timeAnswer.toDateTime()).getDisplayText());
        });
    }

    @Test
    public void clickingButton_callsSetWidgetWaitingForData() {
        FormEntryPrompt prompt = promptWithQuestionDefAndAnswer(questionDef, null);
        TimeWidget widget = createWidget(prompt);
        widget.binding.widgetButton.performClick();

        verify(dateTimeWidgetListener).setWidgetWaitingForData(prompt.getIndex());
    }

    @Test
    public void clickingButton_callsDisplayTimePickerDialogWithCurrentTime_whenPromptDoesNotHaveAnswer() {
        FormEntryPrompt prompt = promptWithQuestionDefAndAnswer(questionDef, null);
        TimeWidget widget = createWidget(prompt);
        widget.binding.widgetButton.performClick();

        verify(dateTimeWidgetListener).displayTimePickerDialog(widgetActivity, DateTimeWidgetUtils.getCurrentDateTime());
    }

    @Test
    public void clickingButton_callsDisplayTimePickerDialogWithSelectedTime_whenPromptHasAnswer() {
        FormEntryPrompt prompt = promptWithQuestionDefAndAnswer(questionDef, new TimeData(timeAnswer.toDateTime().toDate()));
        TimeWidget widget = createWidget(prompt);
        widget.binding.widgetButton.performClick();

        verify(dateTimeWidgetListener).displayTimePickerDialog(widgetActivity, timeAnswer);
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new TimeData(timeAnswer.toDateTime().toDate())));
        widget.clearAnswer();
        assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_time_selected));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new TimeData(timeAnswer.toDateTime().toDate())));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonForLong_callsLongClickListener() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        widget.setOnLongClickListener(onLongClickListener);
        widget.binding.widgetButton.performLongClick();

        verify(onLongClickListener).onLongClick(widget.binding.widgetButton);
    }

    @Test
    public void clickingAnswerTextViewForLong_callsLongClickListener() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        widget.setOnLongClickListener(onLongClickListener);
        widget.binding.widgetAnswerText.performLongClick();

        verify(onLongClickListener).onLongClick(widget.binding.widgetAnswerText);
    }

    private TimeWidget createWidget(FormEntryPrompt prompt) {
        return new TimeWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"), fakeLifecycleOwner, dateTimeWidgetListener);
    }
}
