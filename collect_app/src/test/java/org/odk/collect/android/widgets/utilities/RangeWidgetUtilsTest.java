package org.odk.collect.android.widgets.utilities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

import android.content.Context;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.StringData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.databinding.RangePickerWidgetAnswerBinding;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.support.WidgetTestActivity;
import org.odk.collect.testshared.RobolectricHelpers;
import org.robolectric.shadows.ShadowToast;

import java.math.BigDecimal;

@RunWith(AndroidJUnit4.class)
public class RangeWidgetUtilsTest {
    private RangePickerWidgetAnswerBinding binding;
    private RangeQuestion rangeQuestion;
    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        context.setTheme(com.google.android.material.R.style.Theme_MaterialComponents);

        binding = RangePickerWidgetAnswerBinding.inflate((widgetTestActivity()).getLayoutInflater());

        rangeQuestion = mock(RangeQuestion.class);
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.ONE);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ONE);
    }

    @Test
    public void usingReadOnlyOption_hidesPickerButton() {
        RangeWidgetUtils.setUpRangePickerWidget(widgetTestActivity(), binding, promptWithReadOnlyAndQuestionDef(rangeQuestion));
        assertThat(binding.widgetButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_answerTextViewShowsNoValueSelected() {
        RangeWidgetUtils.setUpRangePickerWidget(widgetTestActivity(), binding, promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertThat(binding.widgetAnswerText.getText(), equalTo(widgetTestActivity().getString(org.odk.collect.strings.R.string.no_value_selected)));
    }

    @Test
    public void whenPromptHasAnswer_answerTextViewShowsCorrectAnswer() {
        RangeWidgetUtils.setUpRangePickerWidget(widgetTestActivity(), binding, promptWithQuestionDefAndAnswer(
                rangeQuestion, new StringData("4")));
        assertThat(binding.widgetAnswerText.getText(), equalTo("4"));
    }

    @Test
    public void whenPromptDoesNotHaveAnswer_pickerButtonShowsNoValueSelected() {
        RangeWidgetUtils.setUpRangePickerWidget(widgetTestActivity(), binding, promptWithQuestionDefAndAnswer(rangeQuestion, null));
        assertThat(binding.widgetButton.getText(), equalTo(widgetTestActivity().getString(org.odk.collect.strings.R.string.select_value)));
    }

    @Test
    public void whenPromptHasAnswer_pickerButtonShowsCorrectAnswer() {
        RangeWidgetUtils.setUpRangePickerWidget(widgetTestActivity(), binding, promptWithQuestionDefAndAnswer(
                rangeQuestion, new StringData("4")));
        assertThat(binding.widgetButton.getText(), equalTo(widgetTestActivity().getString(org.odk.collect.strings.R.string.edit_value)));
    }

    @Test
    public void setNumberPickerValue_updatesAnswer() {
        RangeWidgetUtils.getNumberPickerProgress(binding, rangeQuestion.getRangeStart(), rangeQuestion.getRangeStep(), rangeQuestion.getRangeEnd(), 4);
        assertThat(binding.widgetAnswerText.getText(), equalTo("6"));
    }

    @Test
    public void setNumberPickerValue_whenRangeStartIsGreaterThenRangeEnd_updatesAnswer() {
        when(rangeQuestion.getRangeStart()).thenReturn(BigDecimal.TEN);
        when(rangeQuestion.getRangeEnd()).thenReturn(BigDecimal.ONE);

        RangeWidgetUtils.getNumberPickerProgress(binding, rangeQuestion.getRangeStart(), rangeQuestion.getRangeStep(), rangeQuestion.getRangeEnd(), 4);
        assertThat(binding.widgetAnswerText.getText(), equalTo("5"));
    }

    @Test
    public void whenRangeQuestionHasZeroRangeStep_invalidWidgetToastIsShown() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ZERO);
        assertThat(RangeWidgetUtils.isWidgetValid(context, rangeQuestion), equalTo(false));

        String toastText = ShadowToast.getTextOfLatestToast();
        assertThat(toastText, equalTo(ApplicationProvider.getApplicationContext().getString(org.odk.collect.strings.R.string.invalid_range_widget)));
    }

    @Test
    public void whenPromptHasInvalidWidgetParameters_invalidWidgetToastIsShown() {
        when(rangeQuestion.getRangeStep()).thenReturn(new BigDecimal(2));
        assertThat(RangeWidgetUtils.isWidgetValid(context, rangeQuestion), equalTo(false));

        String toastText = ShadowToast.getTextOfLatestToast();
        assertThat(toastText, equalTo(ApplicationProvider.getApplicationContext().getString(org.odk.collect.strings.R.string.invalid_range_widget)));
    }

    @Test
    public void whenRangeQuestionHasZeroRangeStep_pickerButtonIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(BigDecimal.ZERO);
        RangeWidgetUtils.setUpRangePickerWidget(widgetTestActivity(), binding, promptWithReadOnlyAndQuestionDef(rangeQuestion));
        assertFalse(binding.widgetButton.isEnabled());
    }

    @Test
    public void whenPromptHasInvalidWidgetParameters_pickerButtonIsDisabled() {
        when(rangeQuestion.getRangeStep()).thenReturn(new BigDecimal(2));
        RangeWidgetUtils.setUpRangePickerWidget(widgetTestActivity(), binding, promptWithReadOnlyAndQuestionDef(rangeQuestion));
        assertFalse(binding.widgetButton.isEnabled());
    }

    @Test
    public void clickingPickerButton_showsNumberPickerDialog() {
        WidgetTestActivity activity = CollectHelpers.createThemedActivity(WidgetTestActivity.class);
        RangeWidgetUtils.showNumberPickerDialog(activity, new String[]{"1", "2", "3"}, 0, 0);
        RobolectricHelpers.runLooper();
        NumberPickerDialog numberPickerDialog = (NumberPickerDialog) activity.getSupportFragmentManager()
                .findFragmentByTag(NumberPickerDialog.NUMBER_PICKER_DIALOG_TAG);

        assertNotNull(numberPickerDialog);
    }
}
