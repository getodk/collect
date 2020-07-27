package org.odk.collect.android.widgets;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithRangeQuestionAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndRangeQuestion;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

@RunWith(RobolectricTestRunner.class)
public class RangeIntegerWidgetTest {

    private RangeQuestion rangeQuestion;

    @Before
    public void setup() {
        rangeQuestion = mock(RangeQuestion.class);

        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.ONE);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ONE);
    }

    @Test
    public void getAnswer_whenPromptAnswerDoesNotHaveAnswer_returnsZero() {
        assertThat(createWidget(promptWithReadOnlyAndRangeQuestion(rangeQuestion)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, new StringData("4")));
        assertThat(widget.getAnswer().getValue(), equalTo(4));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_sliderIsSetOnStartingIndex() {
        RangeIntegerWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        assertThat(widget.slider.getValue(), equalTo(0.0F));
    }

    @Test
    public void whenPromptHasAnswer_sliderShouldShowCorrectAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, new StringData("4")));
        assertThat(widget.slider.getValue(), equalTo(3.0F));
    }

    @Test
    public void whenPromptHasRangeStepAsZero_invalidWidgetToastIsShownAndWidgetIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ZERO);
        RangeIntegerWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        String toastText = ShadowToast.getTextOfLatestToast();

        assertThat(widget.slider.isEnabled(), equalTo(false));
        assertThat(toastText, equalTo(widget.getContext().getString(R.string.invalid_range_widget)));
    }

    @Test
    public void whenPromptHasInvalidWidgetParameters_invalidWidgetToastIsShownAndWidgetIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(new BigDecimal(2));
        RangeIntegerWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        String toastText = ShadowToast.getTextOfLatestToast();

        assertThat(widget.slider.isEnabled(), equalTo(false));
        assertThat(toastText, equalTo(widget.getContext().getString(R.string.invalid_range_widget)));
    }

    @Test
    public void sliderShouldShowCorrectAppearance() {
        RangeIntegerWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));

        assertThat(widget.slider.getValueFrom(), equalTo(1.0F));
        assertThat(widget.slider.getValueTo(), equalTo(10.0F));
        assertThat(widget.slider.getStepSize(), equalTo(1.0F));
    }

    @Test
    public void changingSliderValue_shouldUpdateAnswer() {
        RangeIntegerWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        widget.slider.setValue(4.0F);

        assertThat(widget.getAnswer().getValue(), equalTo(5));
    }

    @Test
    public void changingSliderValue_callsValueChangeListener() {
        RangeIntegerWidget widget = createWidget(promptWithRangeQuestionAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);
        widget.slider.setValue(4.0F);

        verify(valueChangedListener).widgetValueChanged(widget);
    }

    private RangeIntegerWidget createWidget(FormEntryPrompt prompt) {
        return new RangeIntegerWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
