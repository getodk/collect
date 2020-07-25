package org.odk.collect.android.widgets;

import android.app.Application;
import android.util.Range;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.widgets.base.QuestionWidgetTest;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class RatingWidgetTest {

    private RangeQuestion rangeQuestion;

    @Before
    public void setup() {
        rangeQuestion = mock(RangeQuestion.class);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.valueOf(3));
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithAnswer(null)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RatingWidget widget = createWidget(promptWithAnswer(new StringData("1")));
        assertThat(widget.getAnswer().getValue(), equalTo(1));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RatingWidget widget = createWidget(promptWithAnswer(new StringData("1")));

        widget.clearAnswer();
        assertThat(widget.getAnswer(), nullValue());
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        RatingWidget widget = createWidget(promptWithAnswer(new StringData("1")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setValueChangedListener(valueChangedListener);

        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        RatingWidget widget = createWidget(promptWithReadOnly());

        for (int i = 0; i < widget.gridLayout.getChildCount(); i++) {
            assertThat(widget.gridLayout.getChildAt(i).getVisibility(), is(View.VISIBLE));
            assertThat(widget.gridLayout.getChildAt(i).isEnabled(), is(Boolean.FALSE));
        }
    }

    private RatingWidget createWidget(FormEntryPrompt prompt) {
        return new RatingWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"), rangeQuestion);
    }

  /*  private final IntegerData answer = new IntegerData(4);

    @Before
    public void setUp() throws Exception {
        super.setUp();

        RangeQuestion rangeQuestion = new RangeQuestion();
        rangeQuestion.setRangeEnd(new BigDecimal(5));

        when(formEntryPrompt.getQuestion()).thenReturn(rangeQuestion);
    }

    @NonNull
    @Override
    public RatingWidget createWidget() {
        return new RatingWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @NonNull
    @Override
    public IntegerData getNextAnswer() {
        return answer;
    }

    @Override
    public void getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer() {
        getSpyWidget().answer = (Integer) answer.getValue();
        super.getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer();
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        for (int i = 0; i < getSpyWidget().gridLayout.getChildCount(); i++) {
            assertThat(getSpyWidget().gridLayout.getChildAt(i).getVisibility(), is(View.VISIBLE));
            assertThat(getSpyWidget().gridLayout.getChildAt(i).isEnabled(), is(Boolean.FALSE));
        }
    }*/
}
