package org.odk.collect.android.widgets;

import android.view.View;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowToast;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithRangeQuestionAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndRangeQuestion;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

/**
 * @author James Knight
 */

@RunWith(RobolectricTestRunner.class)
public class RangeDecimalWidgetTest {

    private RangeQuestion rangeQuestion;

    @Before
    public void setup() {
        rangeQuestion = mock(RangeQuestion.class);

        when(rangeQuestion.getRangeStart()).thenReturn(new BigDecimal("1.5"));
        when(rangeQuestion.getRangeEnd()).thenReturn(new BigDecimal("5.5"));
        when(rangeQuestion.getRangeStep()).thenReturn(new BigDecimal("0.5"));
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsZero() {
        assertThat(createWidget(promptWithReadOnlyAndRangeQuestion(rangeQuestion)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.getAnswer().getValue(), equalTo(2.5));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_sliderIsSetOnStartingIndex() {
        RangeDecimalWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        assertThat(widget.getSlider().getValue(), equalTo(1.5F));
        assertThat(widget.getCurrentValue().getText(), equalTo(""));
    }

    @Test
    public void whenPromptHasAnswer_sliderShouldShowCorrectAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.getSlider().getValue(), equalTo(2.5F));
        assertThat(widget.getCurrentValue().getText(), equalTo("2.5"));
    }

    @Test
    public void whenPromptHasRangeStepAsZero_invalidWidgetToastIsShownAndWidgetIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ZERO);
        RangeDecimalWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        String toastText = ShadowToast.getTextOfLatestToast();

        assertThat(widget.getSlider().isEnabled(), equalTo(false));
        assertThat(toastText, equalTo(widget.getContext().getString(R.string.invalid_range_widget)));
    }

    @Test
    public void whenPromptHasInvalidWidgetParameters_invalidWidgetToastIsShownAndWidgetIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(new BigDecimal(3));
        RangeDecimalWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        String toastText = ShadowToast.getTextOfLatestToast();

        assertThat(widget.getSlider().isEnabled(), equalTo(false));
        assertThat(toastText, equalTo(widget.getContext().getString(R.string.invalid_range_widget)));
    }

    @Test
    public void sliderShouldShowCorrectAppearance() {
        RangeDecimalWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));

        assertThat(widget.getMinValue().getText(), equalTo("1.5"));
        assertThat(widget.getMaxValue().getText(), equalTo("5.5"));

        assertThat(widget.getSlider().getValueFrom(), equalTo(1.5F));
        assertThat(widget.getSlider().getValueTo(), equalTo(5.5F));
        assertThat(widget.getSlider().getStepSize(), equalTo(0.5F));
    }


    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, new StringData("2.5")));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(widget.getCurrentValue().getText(), equalTo(""));
        assertThat(widget.getSlider().getValue(), equalTo(1.5F));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        RangeDecimalWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);

        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void changingSliderValue_updatesAnswer() {
        RangeDecimalWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        widget.getSlider().setValue(2.5F);

        assertThat(widget.getAnswer().getValue(), equalTo(2.5));
        assertThat(widget.getCurrentValue().getText(), equalTo("2.5"));
    }

    @Test
    public void changingSliderValue_callsValueChangeListener() {
        RangeDecimalWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.getSlider().setValue(2.5F);

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingSliderForLong_doesNotCallLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);

        RangeDecimalWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        widget.setOnLongClickListener(listener);
        widget.getSlider().performLongClick();

        verify(listener, never()).onLongClick(widget.getSlider());
    }

    private RangeDecimalWidget createWidget(FormEntryPrompt prompt) {
        return new RangeDecimalWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
