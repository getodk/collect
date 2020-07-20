package org.odk.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class TimeWidgetTest {

    private QuestionDef questionDef;
    private View.OnLongClickListener listener;
    private DateTime dateTime;

    @Before
    public void setUp() {
        questionDef = mock(QuestionDef.class);
        listener = mock(View.OnLongClickListener.class);

        dateTime = new DateTime().withTime(12, 0, 0, 0);
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
    public void whenPromptDoesNotHaveAnswer_answerTextViewShowsNoDateSelected() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_time_selected));
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectDate() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new TimeData(dateTime.toDate())));
        assertEquals(widget.binding.widgetAnswerText.getText(), "12:00");
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new TimeData(dateTime.toDate())));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_time_selected));
    }

    @Test
    public void clearAnswer_callValueChangeListener() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new TimeData(dateTime.toDate())));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingButtonForLong_callsLongClickListener() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        widget.setOnLongClickListener(listener);
        widget.binding.widgetButton.performLongClick();

        verify(listener).onLongClick(widget.binding.widgetButton);
    }

    @Test
    public void clickingAnswerTextViewForLong_callsLongClickListener() {
        TimeWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        widget.setOnLongClickListener(listener);
        widget.binding.widgetAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.widgetAnswerText);
    }

    private TimeWidget createWidget(FormEntryPrompt prompt) {
        return new TimeWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
