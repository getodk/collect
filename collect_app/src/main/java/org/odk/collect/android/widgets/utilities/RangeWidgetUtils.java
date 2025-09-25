package org.odk.collect.android.widgets.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.databinding.RangePickerWidgetAnswerBinding;
import org.odk.collect.android.databinding.RangeWidgetHorizontalBinding;
import org.odk.collect.android.databinding.RangeWidgetVerticalBinding;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.views.TrackingTouchSlider;
import org.odk.collect.androidshared.ui.ToastUtils;

import java.math.BigDecimal;

import timber.log.Timber;

public class RangeWidgetUtils {
    private static final String VERTICAL_APPEARANCE = "vertical";
    private static final String NO_TICKS_APPEARANCE = "no-ticks";

    private RangeWidgetUtils() {
    }

    public static class RangeWidgetLayoutElements {
        private final View answerView;
        private final TrackingTouchSlider slider;
        private final TextView currentValue;

        public RangeWidgetLayoutElements(View answerView, TrackingTouchSlider slider, TextView currentValue) {
            this.answerView = answerView;
            this.slider = slider;
            this.currentValue = currentValue;
        }

        public View getAnswerView() {
            return answerView;
        }

        public TrackingTouchSlider getSlider() {
            return slider;
        }

        public TextView getCurrentValue() {
            return currentValue;
        }
    }

    public static RangeWidgetLayoutElements setUpLayoutElements(Context context, FormEntryPrompt prompt) {
        View answerView;
        TrackingTouchSlider slider;
        TextView currentValue;
        TextView minValue;
        TextView maxValue;

        String appearance = prompt.getAppearanceHint();

        if (appearance != null && appearance.contains(VERTICAL_APPEARANCE)) {
            RangeWidgetVerticalBinding rangeWidgetVerticalBinding = RangeWidgetVerticalBinding
                    .inflate(((Activity) context).getLayoutInflater());
            answerView = rangeWidgetVerticalBinding.getRoot();

            slider = rangeWidgetVerticalBinding.slider;
            currentValue = rangeWidgetVerticalBinding.currentValue;
            minValue = rangeWidgetVerticalBinding.minValue;
            maxValue = rangeWidgetVerticalBinding.maxValue;
        } else {
            RangeWidgetHorizontalBinding rangeWidgetHorizontalBinding = RangeWidgetHorizontalBinding
                    .inflate(((Activity) context).getLayoutInflater());
            answerView = rangeWidgetHorizontalBinding.getRoot();

            slider = rangeWidgetHorizontalBinding.slider;
            currentValue = rangeWidgetHorizontalBinding.currentValue;
            minValue = rangeWidgetHorizontalBinding.minValue;
            maxValue = rangeWidgetHorizontalBinding.maxValue;
        }

        setUpWidgetParameters((RangeQuestion) prompt.getQuestion(), minValue, maxValue);
        if (prompt.isReadOnly()) {
            slider.setEnabled(false);
        }

        slider.setId(View.generateViewId());

        return new RangeWidgetLayoutElements(answerView, slider, currentValue);
    }

    static void setUpWidgetParameters(RangeQuestion rangeQuestion, TextView minValue, TextView maxValue) {
        BigDecimal rangeStart = rangeQuestion.getRangeStart();
        BigDecimal rangeEnd = rangeQuestion.getRangeEnd();

        minValue.setText(String.valueOf(rangeStart));
        maxValue.setText(String.valueOf(rangeEnd));
    }

    @SuppressLint("ClickableViewAccessibility")
    public static BigDecimal setUpSlider(FormEntryPrompt prompt, TrackingTouchSlider slider, boolean isIntegerType) {
        RangeQuestion rangeQuestion = (RangeQuestion) prompt.getQuestion();
        BigDecimal rangeStart = rangeQuestion.getRangeStart();
        BigDecimal rangeEnd = rangeQuestion.getRangeEnd();
        BigDecimal rangeStep = rangeQuestion.getRangeStep().abs() != null ? rangeQuestion.getRangeStep().abs() : BigDecimal.valueOf(0.5);

        BigDecimal actualValue = null;
        if (prompt.getAnswerValue() != null) {
            actualValue = new BigDecimal(prompt.getAnswerValue().getValue().toString());

            if (!isValueInRange(actualValue, rangeStart, rangeEnd)) {
                actualValue = null;
            }
        }

        if (isRangeSliderWidgetValid(rangeQuestion, slider)) {
            if (prompt.getAppearanceHint() != null && prompt.getAppearanceHint().contains(NO_TICKS_APPEARANCE)) {
                slider.setTickVisible(false);
                slider.setTrackStopIndicatorSize(0);
            }

            if (rangeEnd.compareTo(rangeStart) > -1) {
                slider.setValueFrom(rangeStart.floatValue());
                slider.setValueTo(rangeEnd.floatValue());
            } else {
                slider.setValueFrom(rangeEnd.floatValue());
                slider.setValueTo(rangeStart.floatValue());
            }

            if (isIntegerType) {
                slider.setStepSize(rangeStep.intValue());
            } else {
                slider.setStepSize(rangeStep.floatValue());
            }

            if (actualValue != null) {
                if (rangeEnd.compareTo(rangeStart) > -1) {
                    slider.setValue(actualValue.floatValue());
                } else {
                    slider.setValue(rangeStart.add(rangeEnd).subtract(actualValue).floatValue());
                }
            }
        }

        return actualValue;
    }

    private static boolean isValueInRange(BigDecimal value, BigDecimal rangeStart, BigDecimal rangeEnd) {
        return rangeStart.compareTo(rangeEnd) < 0
                ? value.compareTo(rangeStart) > -1 && value.compareTo(rangeEnd) < 1
                : value.compareTo(rangeStart) < 1 && value.compareTo(rangeEnd) > -1;
    }

    public static void setUpRangePickerWidget(Context context, RangePickerWidgetAnswerBinding binding, FormEntryPrompt prompt) {
        if (RangeWidgetUtils.isRangePickerWidgetValid((RangeQuestion) prompt.getQuestion(), binding.widgetButton)) {
            if (prompt.getAnswerValue() != null) {
                BigDecimal actualValue = new BigDecimal(prompt.getAnswerValue().getValue().toString());
                binding.widgetAnswerText.setText(String.valueOf(actualValue));
                binding.widgetButton.setText(context.getString(org.odk.collect.strings.R.string.edit_value));
            }
        }
        if (prompt.isReadOnly()) {
            binding.widgetButton.setVisibility(View.GONE);
        }
    }

    public static Float getActualValue(FormEntryPrompt prompt, float value) {
        RangeQuestion rangeQuestion = (RangeQuestion) prompt.getQuestion();
        Float rangeStart = rangeQuestion.getRangeStart().floatValue();
        Float rangeEnd = rangeQuestion.getRangeEnd().floatValue();

        if (rangeEnd.compareTo(rangeStart) < 0) {
            value = rangeEnd + rangeStart - value;
        }

        return value;
    }

    public static void showNumberPickerDialog(FragmentActivity activity, String[] displayedValuesForNumberPicker, int id, int progress) {
        NumberPickerDialog dialog = NumberPickerDialog.newInstance(id, displayedValuesForNumberPicker, progress);
        try {
            dialog.show(activity.getSupportFragmentManager(), NumberPickerDialog.NUMBER_PICKER_DIALOG_TAG);
        } catch (ClassCastException e) {
            Timber.i(e);
        }
    }

    public static int getNumberPickerProgress(RangePickerWidgetAnswerBinding binding, BigDecimal rangeStart, BigDecimal rangeStep,
                                               BigDecimal rangeEnd, int value) {
        BigDecimal actualValue;
        int elementCount = rangeEnd.subtract(rangeStart).abs().divide(rangeStep).intValue();
        BigDecimal multiply = new BigDecimal(elementCount - value).multiply(rangeStep);

        if (rangeStart.compareTo(rangeEnd) < 0) {
            actualValue = rangeStart.add(multiply);
        } else {
            actualValue = rangeStart.subtract(multiply);
        }
        binding.widgetAnswerText.setText(String.valueOf(actualValue));
        binding.widgetButton.setText(org.odk.collect.strings.R.string.edit_value);

        return actualValue.subtract(rangeStart).abs().divide(rangeStep).intValue();
    }

    public static boolean isRangeSliderWidgetValid(RangeQuestion rangeQuestion, TrackingTouchSlider slider) {
        if (!isWidgetValid(slider.getContext(), rangeQuestion)) {
            slider.setEnabled(false);
        }
        return isWidgetValid(slider.getContext(), rangeQuestion);
    }

    static boolean isWidgetValid(Context context, RangeQuestion rangeQuestion) {
        BigDecimal rangeStart = rangeQuestion.getRangeStart();
        BigDecimal rangeEnd = rangeQuestion.getRangeEnd();
        BigDecimal rangeStep = rangeQuestion.getRangeStep().abs();

        boolean result = true;
        if (rangeStep.compareTo(BigDecimal.ZERO) == 0
                || rangeEnd.subtract(rangeStart).remainder(rangeStep).compareTo(BigDecimal.ZERO) != 0) {
            ToastUtils.showLongToast(org.odk.collect.strings.R.string.invalid_range_widget);
            result = false;
        }
        return result;
    }

    private static boolean isRangePickerWidgetValid(RangeQuestion rangeQuestion, Button widgetButton) {
        if (!isWidgetValid(widgetButton.getContext(), rangeQuestion)) {
            widgetButton.setEnabled(false);
        }
        return isWidgetValid(widgetButton.getContext(), rangeQuestion);
    }
}
