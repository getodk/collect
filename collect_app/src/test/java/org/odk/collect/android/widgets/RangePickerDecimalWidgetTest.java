package org.odk.collect.android.widgets;

import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.listeners.WidgetValueChangedListener;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowToast;

import java.math.BigDecimal;

import static android.os.Looper.getMainLooper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class RangePickerDecimalWidgetTest {

    private TestScreenContextActivity widgetActivity;
    private RangeQuestion rangeQuestion;

    @Before
    public void setup() {
        widgetActivity = widgetTestActivity();
        rangeQuestion = mock(RangeQuestion.class);

        when(rangeQuestion.getRangeStart()).thenReturn(new BigDecimal("1.5"));
        when(rangeQuestion.getRangeEnd()).thenReturn(new BigDecimal("5.5"));
        when(rangeQuestion.getRangeStep()).thenReturn(new BigDecimal("0.5"));
    }

    @Test
    public void whenRangeEndIsGreaterThanRangeStart_widgetsSetsCorrectValuesForNumberPicker() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        int index = 0;

        for (float i = 5.5F; i >= 1.5F; i -= 0.5F, index++) {
            assertThat(widget.displayedValuesForNumberPicker[index], equalTo(String.valueOf(i)));
        }
    }

    @Test
    public void whenRangeStartIsGreaterThanRangeEnd_widgetsSetsCorrectValuesForNumberPicker() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.ONE);
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        int index = 0;

        for (float i = 1.5F; i <= 5.5F; i += 0.5F, index++) {
            assertThat(widget.displayedValuesForNumberPicker[index], equalTo(String.valueOf(i)));
        }
    }

    @Test
    public void usingReadOnly_showsDisabledPickerButton() {
        assertThat(createWidget(promptWithReadOnlyAndQuestionDef(rangeQuestion)).binding.widgetButton.isEnabled(), equalTo(false));
    }

    @Test
    public void getAnswer_whenPromptDoesNotHaveAnswer_returnsNull() {
        assertThat(createWidget(promptWithReadOnlyAndQuestionDef(rangeQuestion)).getAnswer(), nullValue());
    }

    @Test
    public void getAnswer_whenPromptHasAnswer_returnsAnswer() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.getAnswer().getValue(), equalTo(2.5F));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_answerTextViewShowsNoValueSelected() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertThat(widget.binding.widgetAnswerText.getText(), equalTo(widget.getContext().getString(R.string.no_value_selected)));
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectAnswer() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        assertThat(widget.binding.widgetAnswerText.getText(), equalTo("2.5"));
    }

    @Test
    public void clearAnswer_clearsWidgetAnswer() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, new StringData("2.5")));
        widget.clearAnswer();

        assertThat(widget.getAnswer(), nullValue());
        assertThat(widget.binding.widgetAnswerText.getText(), equalTo(widget.getContext().getString(R.string.no_value_selected)));
    }

    @Test
    public void clearAnswer_callsValueChangeListener() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        WidgetValueChangedListener valueChangedListener = mockValueChangedListener(widget);

        widget.clearAnswer();
        verify(valueChangedListener).widgetValueChanged(widget);
    }

    @Test
    public void clickingPickerButton_showsNumberPickerDialog() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.binding.widgetButton.performClick();

        NumberPickerDialog numberPickerDialog = (NumberPickerDialog) widgetActivity.getActivity().getSupportFragmentManager()
                .findFragmentByTag(NumberPickerDialog.NUMBER_PICKER_DIALOG_TAG);
        shadowOf(getMainLooper()).idle();

        assertNotNull(numberPickerDialog);
    }

    @Test
    public void setNumberPickerValue_updatesAnswer() {
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setNumberPickerValue(4);

        assertThat(widget.getAnswer().getValue(), equalTo(6));
        assertThat(widget.binding.widgetAnswerText.getText(), equalTo("6"));
    }

    @Test
    public void setNumberPickerValue_whenRangeStartIsGreaterThenRangeEnd_updatesAnswer() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.ONE);
        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setNumberPickerValue(4);

        assertThat(widget.getAnswer().getValue(), equalTo(5.0F));
        assertThat(widget.binding.widgetAnswerText.getText(), equalTo("5"));
    }

    @Test
    public void clickingWidgetForLong_callsLongClickListener() {
        View.OnLongClickListener listener = mock(View.OnLongClickListener.class);

        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        widget.setOnLongClickListener(listener);
        widget.binding.widgetButton.performLongClick();
        widget.binding.widgetAnswerText.performLongClick();

        verify(listener).onLongClick(widget.binding.widgetButton);
        verify(listener).onLongClick(widget.binding.widgetAnswerText);
    }

    @Test
    public void whenRangeQuestionHasZeroRangeStep_invalidWidgetToastIsShownAndSliderIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ZERO);

        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        String toastText = ShadowToast.getTextOfLatestToast();

        assertThat(widget.binding.widgetButton.isEnabled(), equalTo(false));
        assertThat(toastText, equalTo(ApplicationProvider.getApplicationContext().getString(R.string.invalid_range_widget)));
    }

    @Test
    public void whenPromptHasInvalidWidgetParameters_invalidWidgetToastIsShownAndSliderIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(new BigDecimal(3));

        RangePickerDecimalWidget widget = createWidget(promptWithQuestionDefAndAnswer(rangeQuestion, null));
        String toastText = ShadowToast.getTextOfLatestToast();

        assertThat(widget.binding.widgetButton.isEnabled(), equalTo(false));
        assertThat(toastText, equalTo(ApplicationProvider.getApplicationContext().getString(R.string.invalid_range_widget)));
    }

    private RangePickerDecimalWidget createWidget(FormEntryPrompt prompt) {
        return new RangePickerDecimalWidget(widgetActivity, new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
