package org.odk.collect.android.widgets;

import android.view.View;


import org.javarosa.core.model.QuestionDef;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
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

    @Before
    public void setUp() {
        questionDef = mock(QuestionDef.class);
        listener = mock(View.OnLongClickListener.class);
    }

    @Test
    public void usingReadOnlyOption_doesNotShowButton() {
        DateWidget widget = createWidget(promptWithReadOnlyAndQuestionDef(questionDef));
        assertThat(widget.dateButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithQuestionDefAndAnswer(questionDef, null)), nullValue());
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        DateWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(widget.dateTextView.getText(), equalTo(widget.getContext().getString(R.string.no_date_selected)));
    }

    @Test
    public void clearAnswer_callValueChangeListener() {
        DateWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    public void clickingButtonForLong_callsLongClickListener() {
        DateWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        widget.setOnLongClickListener(listener);
        widget.dateButton.performLongClick();

        verify(listener).onLongClick(widget.dateButton);
    }

    @Test
    public void clickingAnswerTextViewForLong_callsLongClickListener() {
        DateWidget widget = createWidget(promptWithQuestionDefAndAnswer(questionDef, null));
        widget.setOnLongClickListener(listener);
        widget.dateTextView.performLongClick();

        verify(listener).onLongClick(widget.dateTextView);
    }

    private DateWidget createWidget(FormEntryPrompt prompt) {
        return new DateWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}