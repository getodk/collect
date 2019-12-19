package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IntegerData;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.QuestionWidgetTest;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class RatingWidgetTest extends QuestionWidgetTest<RatingWidget, IntegerData> {

    private final IntegerData answer = new IntegerData(4);

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
        getWidget().answer = (Integer) answer.getValue();
        super.getAnswerShouldReturnExistingAnswerIfPromptHasExistingAnswer();
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        for (int i = 0; i < getWidget().gridLayout.getChildCount(); i++) {
            assertThat(getWidget().gridLayout.getChildAt(i).getVisibility(), is(View.VISIBLE));
            assertThat(getWidget().gridLayout.getChildAt(i).isEnabled(), is(Boolean.FALSE));
        }
    }
}
