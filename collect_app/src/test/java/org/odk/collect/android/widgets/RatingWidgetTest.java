package org.odk.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.robolectric.RobolectricTestRunner;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;

import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithRangeQuestionAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndRangeQuestion;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class RatingWidgetTest {

    private RangeQuestion rangeQuestion;

    @Before
    public void setup() {
        rangeQuestion = mock(RangeQuestion.class);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.valueOf(5));
    }

    @Test
    public void ratingBarShowsCorrectNumberOfStars() {
        RatingWidget widget = createWidget(promptWithReadOnlyAndRangeQuestion(rangeQuestion));
        assertThat(widget.getRatingBar().getNumStars(), equalTo(5));
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsZero() {
        assertThat(createWidget(promptWithReadOnlyAndRangeQuestion(rangeQuestion)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RatingWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, new StringData("3")));
        assertThat(widget.getAnswer().getValue(), equalTo(3));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_noStarsAreHighlightedOnRatingBar() {
        RatingWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        assertThat(widget.getRatingBar().getRating(), equalTo(0.0F));
    }

    @Test
    public void whenPromptHasAnswer_correctNumberOfStarsAreHighlightedOnRatingBar() {
        RatingWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, new StringData("3")));
        assertThat(widget.getRatingBar().getRating(), equalTo(3.0F));
    }

    @Test
    public void usingReadOnly_makesAllClickableElementsDisabled() {
        RatingWidget widget = createWidget(promptWithReadOnlyAndRangeQuestion(rangeQuestion));
        assertThat(widget.getRatingBar().isEnabled(), equalTo(false));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RatingWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, new StringData("3")));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(widget.getRatingBar().getRating(), equalTo(0.0F));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        RatingWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, new StringData("3")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setValueChangedListener(valueChangedListener);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void changingRating_callsValueChangeListeners() {
        RatingWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, new StringData("3")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setValueChangedListener(valueChangedListener);
        widget.getRatingBar().setRating(4.0F);

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void changingRating_updatesAnswer() {
        RatingWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, new StringData("3")));
        widget.getRatingBar().setRating(4.0F);
        assertThat(widget.getAnswer().getValue(), equalTo(4));
    }

    @Test
    public void ratingBar_doesNotAllowUserToSetDecimalRating() {
        RatingWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, new StringData("3")));
        widget.getRatingBar().setRating(4.8F);
        assertThat(widget.getAnswer().getValue(), equalTo(5));
    }

    @Test
    public void clickingRatingBarForLong_callsLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);
        RatingWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        widget.setOnLongClickListener(listener);
        widget.getRatingBar().performLongClick();

        verify(listener).onLongClick(widget.getRatingBar());
    }

    private RatingWidget createWidget(FormEntryPrompt prompt) {
        return new RatingWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
