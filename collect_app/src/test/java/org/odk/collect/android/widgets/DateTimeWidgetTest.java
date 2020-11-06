package org.odk.collect.android.widgets;

import android.view.View;

import androidx.lifecycle.MutableLiveData;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.support.FakeLifecycleOwner;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.widgets.interfaces.DateTimeWidgetListener;
import org.odk.collect.android.widgets.viewmodels.DateTimeViewModel;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.RobolectricHelpers.mockViewModelProvider;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class DateTimeWidgetTest {
    private TestScreenContextActivity widgetActivity;
    private DateTimeViewModel dateTimeViewModel;
    private DateTimeWidgetListener dateTimeWidgetListener;
    private View.OnLongClickListener onLongClickListener;

    private QuestionDef questionDef;
    private LocalDateTime date;
    private LocalDateTime localDateTime;

    @Before
    public void setUp() {
        widgetActivity = widgetTestActivity();
        questionDef = mock(QuestionDef.class);
        onLongClickListener = mock(View.OnLongClickListener.class);
        dateTimeWidgetListener = mock(DateTimeWidgetListener.class);
        dateTimeViewModel = mockViewModelProvider(widgetActivity, DateTimeViewModel.class).get(DateTimeViewModel.class);

        when(dateTimeViewModel.getSelectedDate()).thenReturn(new MutableLiveData<>(new LocalDateTime().withDate(0, 1,1)));
        when(dateTimeViewModel.getSelectedTime()).thenReturn(new MutableLiveData<>(new LocalDateTime().withTime(0, 0,0, 0)));

        date = new LocalDateTime().withYear(2010).withMonthOfYear(5).withDayOfMonth(12);
        localDateTime = new LocalDateTime()
                .withYear(2010)
                .withMonthOfYear(5)
                .withDayOfMonth(12)
                .withHourOfDay(12)
                .withMinuteOfHour(10)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowButtons() {
        DateTimeWidget widget = createWidget(promptWithReadOnlyAndQuestionDef(questionDef));

        assertEquals(widget.binding.dateWidget.widgetButton.getVisibility(), View.GONE);
        assertEquals(widget.binding.timeWidget.widgetButton.getVisibility(), View.GONE);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithQuestionDefAndAnswer(questionDef, null)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        DateTimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new DateTimeData(localDateTime.toDate())));
        assertEquals(widget.getAnswer().getDisplayText(), new DateTimeData(localDateTime.toDate()).getDisplayText());
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_answerTextViewShowsCorrectText() {
        DateTimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));

        assertEquals(widget.binding.dateWidget.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_date_selected));
        assertEquals(widget.binding.timeWidget.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_time_selected));
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectDateAndTime() {
        FormEntryPrompt prompt = promptWithQuestionDefAndAnswer(questionDef, new DateTimeData(localDateTime.toDate()));
        DatePickerDetails datePickerDetails = DateTimeUtils.getDatePickerDetails(prompt.getQuestion().getAppearanceAttr());
        DateTimeWidget widget = createWidget(prompt);

        assertEquals(widget.binding.dateWidget.widgetAnswerText.getText(),
                DateTimeUtils.getDateTimeLabel(date.toDate(), datePickerDetails, false, widget.getContext()));
        assertEquals(widget.binding.timeWidget.widgetAnswerText.getText(), "12:10");
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        DateTimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new DateTimeData(localDateTime.toDate())));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertEquals(widget.binding.dateWidget.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_date_selected));
        assertEquals(widget.binding.timeWidget.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_time_selected));
    }

    @Test
    public void clearAnswer_callValueChangeListener() {
        DateTimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new DateTimeData(localDateTime.toDate())));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonForLong_callsLongClickListener() {
        DateTimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        widget.setOnLongClickListener(onLongClickListener);

        widget.binding.dateWidget.widgetButton.performLongClick();
        widget.binding.timeWidget.widgetButton.performLongClick();

        verify(onLongClickListener).onLongClick(widget.binding.dateWidget.widgetButton);
        verify(onLongClickListener).onLongClick(widget.binding.timeWidget.widgetButton);
    }

    @Test
    public void clickingAnswerTextViewForLong_callsLongClickListener() {
        DateTimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        widget.setOnLongClickListener(onLongClickListener);

        widget.binding.dateWidget.widgetAnswerText.performLongClick();
        widget.binding.timeWidget.widgetAnswerText.performLongClick();

        verify(onLongClickListener).onLongClick(widget.binding.dateWidget.widgetAnswerText);
        verify(onLongClickListener).onLongClick(widget.binding.timeWidget.widgetAnswerText);
    }

    private DateTimeWidget createWidget(FormEntryPrompt prompt) {
        return new DateTimeWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"),
                new FakeLifecycleOwner(), dateTimeWidgetListener);
    }
}
