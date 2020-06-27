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
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
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
        RatingWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.getBinding().ratingBar.getNumStars(), equalTo(5));
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsZero() {
        assertThat(createWidget(promptWithAnswer(null)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RatingWidget widget = createWidget(promptWithAnswer(new StringData("3")));
        assertThat(widget.getAnswer().getValue(), equalTo(3));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_noStarsAreHighlightedOnRatingBar() {
        RatingWidget widget = createWidget(promptWithAnswer(null));
        assertThat(widget.getBinding().ratingBar.getRating(), equalTo(0.0F));
    }

    @Test
    public void whenPromptHasAnswer_correctNumberOfStarsAreHighlightedOnRatingBar() {
        RatingWidget widget = createWidget(promptWithAnswer(new StringData(("3"))));
        assertThat(widget.getBinding().ratingBar.getRating(), equalTo(3.0F));
    }

    @Test
    public void usingReadOnly_makesAllClickableElementsDisabled() {
        RatingWidget widget = createWidget(promptWithReadOnly());
        assertThat(widget.getBinding().ratingBar.isEnabled(), equalTo(false));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RatingWidget widget = createWidget(promptWithAnswer(new StringData("3")));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(widget.getBinding().ratingBar.getRating(), equalTo(0.0F));
    }

    @Test
    public void clearAnswer_callsValueChangeListeners() {
        RatingWidget widget = createWidget(promptWithAnswer(new StringData("3")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setValueChangedListener(valueChangedListener);
        widget.clearAnswer();

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void changingRating_callsValueChangeListeners() {
        RatingWidget widget = createWidget(promptWithAnswer(new StringData("3")));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.setValueChangedListener(valueChangedListener);
        widget.getBinding().ratingBar.setRating(4.0F);

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void changingRating_updatesAnswer() {
        RatingWidget widget = createWidget(promptWithAnswer(new StringData("3")));
        widget.getBinding().ratingBar.setRating(4.0F);
        assertThat(widget.getAnswer().getValue(), equalTo(4));
    }

    @Test
    public void ratingBar_doesNotAllowUserToSetDecimalRating() {
        RatingWidget widget = createWidget(promptWithAnswer(new StringData("3")));
        widget.getBinding().ratingBar.setRating(4.8F);
        assertThat(widget.getAnswer().getValue(), equalTo(4));
    }

    @Test
    public void clickingRatingBarForLong_callsLongClickListener() {
        View.OnLongClickListener listener = mock( View.OnLongClickListener.class);
        RatingWidget widget = createWidget(promptWithAnswer(null));
        widget.setOnLongClickListener(listener);
        widget.getBinding().ratingBar.performLongClick();

        verify(listener).onLongClick(widget.getBinding().ratingBar);
    }

    private RatingWidget createWidget(FormEntryPrompt prompt) {
        return new RatingWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"), rangeQuestion);
    }
}
