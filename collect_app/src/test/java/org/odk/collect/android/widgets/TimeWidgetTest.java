package org.odk.collect.android.widgets;

import android.view.View;

import androidx.lifecycle.MutableLiveData;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
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
public class TimeWidgetTest {

    private TestScreenContextActivity widgetActivity;
    private DateTimeViewModel dateTimeViewModel;
    private DateTimeWidgetListener dateTimeWidgetListener;
    private View.OnLongClickListener onLongClickListener;

    private QuestionDef questionDef;
    private DateTime dateTime;

    @Before
    public void setUp() {
        widgetActivity = widgetTestActivity();
        questionDef = mock(QuestionDef.class);
        onLongClickListener = mock(View.OnLongClickListener.class);
        dateTimeWidgetListener = mock(DateTimeWidgetListener.class);
        dateTimeViewModel = mockViewModelProvider(widgetActivity, DateTimeViewModel.class).get(DateTimeViewModel.class);

        when(dateTimeViewModel.getSelectedTime()).thenReturn(new MutableLiveData<>(new LocalDateTime().withTime(0, 0,0, 0)));
        dateTime = new DateTime().withTime(12, 10, 0, 0);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowButton() {
        TimeWidget widget = createWidget(promptWithReadOnlyAndQuestionDef(questionDef));
        assertEquals(widget.binding.widgetButton.getVisibility(), View.GONE);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithQuestionDefAndAnswer(questionDef, null)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsTime() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new TimeData(dateTime.toDate())));
        assertEquals(widget.getAnswer().getDisplayText(), new TimeData(dateTime.toDate()).getDisplayText());
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_answerTextViewShowsNoTimeSelected() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_time_selected));
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectTime() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new TimeData(dateTime.toDate())));
        assertEquals(widget.binding.widgetAnswerText.getText(), "12:10");
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new TimeData(dateTime.toDate())));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_time_selected));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new TimeData(dateTime.toDate())));
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
        return new TimeWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"),
                new FakeLifecycleOwner(), dateTimeWidgetListener);
    }
}
