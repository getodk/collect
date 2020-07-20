package org.odk.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;
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
public class DateWidgetTest {

    private QuestionDef questionDef;
    private View.OnLongClickListener listener;
    private LocalDateTime date;

    @Before
    public void setUp() {
        questionDef = mock(QuestionDef.class);
        listener = mock(View.OnLongClickListener.class);

        date = new LocalDateTime().withYear(2010).withMonthOfYear(5).withDayOfMonth(12);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowButton() {
        DateWidget widget = createWidget(promptWithReadOnlyAndQuestionDef(questionDef));
        assertEquals(widget.binding.widgetButton.getVisibility(), View.GONE);
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithQuestionDefAndAnswer(questionDef, null)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsDate() {
        DateWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new DateData(date.toDate())));
        assertEquals(widget.getAnswer().getDisplayText(), new DateData(date.toDate()).getDisplayText());
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_answerTextViewShowsNoDateSelected() {
        DateWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_date_selected));
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectDate() {
        FormEntryPrompt prompt = promptWithQuestionDefAndAnswer(questionDef, new DateData(date.toDate()));
        DatePickerDetails datePickerDetails = DateTimeUtils.getDatePickerDetails(prompt.getQuestion().getAppearanceAttr());
        DateWidget widget = createWidget(prompt);

        assertEquals(widget.binding.widgetAnswerText.getText(),
                DateTimeUtils.getDateTimeLabel(date.toDate(), datePickerDetails, false, widget.getContext()));
    }

    @Test
    public void setBinaryData_setsCorrectDateInAnswerTextView() {
        FormEntryPrompt prompt = promptWithQuestionDefAndAnswer(questionDef, null);
        DatePickerDetails datePickerDetails = DateTimeUtils.getDatePickerDetails(prompt.getQuestion().getAppearanceAttr());

        DateWidget widget = createWidget(prompt);
        widget.setBinaryData(date);

        assertEquals(widget.binding.widgetAnswerText.getText(),
                DateTimeUtils.getDateTimeLabel(date.toDate(), datePickerDetails, false, widget.getContext()));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        DateWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertEquals(widget.binding.widgetAnswerText.getText(), widget.getContext().getString(R.string.no_date_selected));
    }

    @Test
    public void clearAnswer_callValueChangeListener() {
        DateWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new DateData(date.toDate())));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    public void clickingButtonForLong_callsLongClickListener() {
        DateWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, new DateData(date.toDate())));
        widget.setOnLongClickListener(listener);
        widget.binding.widgetButton.performLongClick();

        verify(listener).onLongClick(widget.binding.widgetButton);
    }

    @Test
    public void clickingAnswerTextViewForLong_callsLongClickListener() {
        DateWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        widget.setOnLongClickListener(listener);
        widget.binding.widgetAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.widgetAnswerText);
    }

    private DateWidget createWidget(FormEntryPrompt prompt) {
        return new DateWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}