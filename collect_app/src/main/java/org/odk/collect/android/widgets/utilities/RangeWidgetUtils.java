package org.odk.collect.android.widgets.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.databinding.RangeWidgetHorizontalBinding;
import org.odk.collect.android.databinding.RangeWidgetVerticalBinding;
import org.odk.collect.android.databinding.WidgetAnswerBinding;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.views.TrackingTouchSlider;

import java.math.BigDecimal;

import timber.log.Timber;

public class RangeWidgetUtils {
    private static final String VERTICAL_APPEARANCE = "vertical";

    private RangeWidgetUtils() {
    }

    public static class RangeWidgetLayoutElements {
        View answerView;
        TrackingTouchSlider slider;
        TextView currentValue;

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

        String appearance = prompt.getQuestion().getAppearanceAttr();

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

        return new RangeWidgetLayoutElements(answerView, slider, currentValue);
    }

    static void setUpWidgetParameters(RangeQuestion rangeQuestion, TextView minValue, TextView maxValue) {
        minValue.setText(String.valueOf(rangeQuestion.getRangeStart()));
        maxValue.setText(String.valueOf(rangeQuestion.getRangeEnd()));
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void setUpSlider(RangeQuestion rangeQuestion, TrackingTouchSlider slider, BigDecimal actualValue) {
        BigDecimal rangeStart = rangeQuestion.getRangeStart();
        BigDecimal rangeEnd = rangeQuestion.getRangeEnd();
        BigDecimal rangeStep = rangeQuestion.getRangeStep().abs();

        slider.setValueFrom(rangeStart.floatValue());
        slider.setValueTo(rangeEnd.floatValue());
        slider.setStepSize(rangeStep.floatValue() != 0 ? rangeStep.floatValue() : 0.5F);
        slider.setValue(actualValue == null ? rangeStart.floatValue() : actualValue.floatValue());

        slider.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            v.onTouchEvent(event);
            return true;
        });
    }

    public static BigDecimal setUpRangePickerWidget(Context context, WidgetAnswerBinding binding, FormEntryPrompt prompt) {
        RangeQuestion rangeQuestion = (RangeQuestion) prompt.getQuestion();
        BigDecimal actualValue = null;

        if (RangeWidgetUtils.isRangePickerWidgetValid(rangeQuestion, binding.widgetButton)) {
            if (prompt.getAnswerValue() != null) {
                actualValue = new BigDecimal(prompt.getAnswerValue().getValue().toString());

                binding.widgetAnswerText.setText(String.valueOf(actualValue));
                binding.widgetButton.setText(context.getString(R.string.edit_value));
            } else {
                setUpNullValueForRangePicker(binding);
                binding.widgetButton.setText(context.getString(R.string.select_value));
            }
        }

        if (prompt.isReadOnly()) {
            binding.widgetButton.setEnabled(false);
        }

        return actualValue;
    }

    public static BigDecimal setUpNullValue(TrackingTouchSlider slider, TextView currentValue) {
        slider.setValue(slider.getValueFrom());
        currentValue.setText("");
        return null;
    }

    public static BigDecimal setUpNullValueForRangePicker(WidgetAnswerBinding binding) {
        binding.widgetAnswerText.setText(R.string.no_value_selected);
        binding.widgetButton.setText(R.string.select_value);
        return null;
    }

    public static String[] setUpDisplayedValuesForNumberPicker(BigDecimal rangeStart, BigDecimal rangeStep, BigDecimal rangeEnd, Boolean isIntegerDataType) {
        int index = 0;
        int elementCount = rangeEnd.subtract(rangeStart).abs().divide(rangeStep).intValue();
        String[] displayedValuesForNumberPicker = new String[elementCount + 1];

        if (rangeEnd.compareTo(rangeStart) > -1) {
            for (BigDecimal i = rangeEnd; i.compareTo(rangeStart) > -1; i = i.subtract(rangeStep.abs())) {
                displayedValuesForNumberPicker[index] = getDisplayValue(i, isIntegerDataType);
                index++;
            }
        } else {
            for (BigDecimal i = rangeEnd; i.compareTo(rangeStart) < 1; i = i.add(rangeStep.abs())) {
                displayedValuesForNumberPicker[index] = getDisplayValue(i, isIntegerDataType);
                index++;
            }
        }
        return displayedValuesForNumberPicker;
    }

    private static String getDisplayValue(BigDecimal value, Boolean isIntegerDataType) {
        if (isIntegerDataType) {
            int intValue = value.intValue();
            return String.valueOf(intValue);
        } else {
            return String.valueOf(value.doubleValue());
        }
    }

    public static void showNumberPickerDialog(FormEntryActivity activity, String[] displayedValuesForNumberPicker, int id, int progress) {
        NumberPickerDialog dialog = NumberPickerDialog.newInstance(id, displayedValuesForNumberPicker, progress);

        try {
            dialog.show(activity.getSupportFragmentManager(), NumberPickerDialog.NUMBER_PICKER_DIALOG_TAG);
        } catch (ClassCastException e) {
            Timber.i(e);
        }
    }

    public static int getNumberPickerProgress(WidgetAnswerBinding binding, BigDecimal rangeStart, BigDecimal rangeStep,
                                              BigDecimal rangeEnd, int value) {

        int elementCount = rangeEnd.subtract(rangeStart).abs().divide(rangeStep).intValue();

        BigDecimal actualValue;
        BigDecimal multiply = new BigDecimal(elementCount - value).multiply(rangeStep);

        if (rangeStart.compareTo(rangeEnd) == -1) {
            actualValue = rangeStart.add(multiply);
        } else {
            actualValue = rangeStart.subtract(multiply);
        }
        binding.widgetAnswerText.setText(String.valueOf(actualValue));
        binding.widgetButton.setText(R.string.edit_value);

        return actualValue.subtract(rangeStart).abs().divide(rangeStep).intValue();
    }

    public static boolean isWidgetValid(RangeQuestion rangeQuestion, TrackingTouchSlider slider) {
        if (!checkWidgetValid(rangeQuestion)) {
            slider.setEnabled(false);
        }
        return checkWidgetValid(rangeQuestion);
    }

    static boolean checkWidgetValid(RangeQuestion rangeQuestion) {
        BigDecimal rangeStart = rangeQuestion.getRangeStart();
        BigDecimal rangeEnd = rangeQuestion.getRangeEnd();
        BigDecimal rangeStep = rangeQuestion.getRangeStep().abs();

        boolean result = true;
        if (rangeStep.compareTo(BigDecimal.ZERO) == 0
                || rangeEnd.subtract(rangeStart).remainder(rangeStep).compareTo(BigDecimal.ZERO) != 0) {
            ToastUtils.showLongToast(R.string.invalid_range_widget);
            result = false;
        }
        return result;
    }

    private static boolean isRangePickerWidgetValid(RangeQuestion rangeQuestion, Button widgetButton) {
        if (!checkWidgetValid(rangeQuestion)) {
            widgetButton.setEnabled(false);
        }
        return checkWidgetValid(rangeQuestion);
    }
}
