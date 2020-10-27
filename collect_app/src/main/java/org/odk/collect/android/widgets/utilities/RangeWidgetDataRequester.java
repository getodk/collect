package org.odk.collect.android.widgets.utilities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.RangeQuestion;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.databinding.RangeWidgetHorizontalBinding;
import org.odk.collect.android.databinding.RangeWidgetVerticalBinding;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.views.TrackingTouchSlider;

import java.math.BigDecimal;

import static org.odk.collect.android.utilities.WidgetAppearanceUtils.NO_TICKS_APPEARANCE;
import static org.odk.collect.android.utilities.WidgetAppearanceUtils.VERTICAL_APPEARANCE;

public class RangeWidgetDataRequester {

    private RangeWidgetDataRequester() {
    }

    public static class RangeWidgetLayoutElements {
        private final View answerView;
        private final TrackingTouchSlider slider;
        private final TextView currentValue;
        private final TextView minValue;
        private final TextView maxValue;

        public RangeWidgetLayoutElements(View answerView, TrackingTouchSlider slider,
                                         TextView currentValue, TextView minValue, TextView maxValue) {
            this.answerView = answerView;
            this.slider = slider;
            this.currentValue = currentValue;
            this.minValue = minValue;
            this.maxValue = maxValue;
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

        public TextView getMinValue() {
            return minValue;
        }

        public TextView getMaxValue() {
            return maxValue;
        }
    }

    public static RangeWidgetLayoutElements getLayoutElements(Context context, FormEntryPrompt prompt) {
        View answerView;
        TrackingTouchSlider slider;
        TextView currentValue;
        TextView minValue;
        TextView maxValue;

        if (WidgetAppearanceUtils.hasAppearance(prompt, VERTICAL_APPEARANCE)) {
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
        return new RangeWidgetLayoutElements(answerView, slider, currentValue, minValue, maxValue);
    }

    public static BigDecimal getActualValue(FormEntryPrompt prompt, Slider slider, BigDecimal rangeStart,
                                            BigDecimal rangeEnd, BigDecimal rangeStep, BigDecimal actualValue) {

        BigDecimal startValue =  rangeEnd.compareTo(rangeStart) < 0 ? rangeEnd : rangeStart;

        if (WidgetAppearanceUtils.hasAppearance(prompt, NO_TICKS_APPEARANCE)) {
            int progress = (actualValue.subtract(startValue).abs().divide(rangeStep)).intValue();
            actualValue = startValue.add(startValue.multiply(BigDecimal.valueOf(progress)));

            slider.setValue(actualValue.floatValue());
        }

        if (rangeEnd.compareTo(rangeStart) < 0) {
            actualValue = rangeEnd.add(rangeStart).subtract(actualValue);
        }
        return actualValue;
    }

    public static String[] getDisplayedValuesForNumberPicker(BigDecimal rangeStart, BigDecimal rangeStep,
                                                             BigDecimal rangeEnd, Boolean isIntegerDataType) {
        int index = 0;
        int elementCount = rangeEnd.subtract(rangeStart).abs().divide(rangeStep).intValue();
        String[] displayedValuesForNumberPicker = new String[elementCount + 1];

        if (rangeEnd.compareTo(rangeStart) > -1) {
            for (BigDecimal i = rangeEnd; i.compareTo(rangeStart) > -1; i = i.subtract(rangeStep.abs())) {
                displayedValuesForNumberPicker[index] = isIntegerDataType ? String.valueOf(i.intValue())
                        : String.valueOf(i.doubleValue());
                index++;
            }
        } else {
            for (BigDecimal i = rangeEnd; i.compareTo(rangeStart) < 1; i = i.add(rangeStep.abs())) {
                displayedValuesForNumberPicker[index] = isIntegerDataType ? String.valueOf(i.intValue())
                        : String.valueOf(i.doubleValue());
                index++;
            }
        }
        return displayedValuesForNumberPicker;
    }

    public static void requestRangePickerValue(Context context, WaitingForDataRegistry waitingForDataRegistry, FormIndex formIndex,
                                               String[] displayedValuesForNumberPicker, int progress) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(NumberPickerDialog.DISPLAYED_VALUES, displayedValuesForNumberPicker);
        bundle.putInt(NumberPickerDialog.PROGRESS, progress);

        DialogUtils.showIfNotShowing(NumberPickerDialog.class, bundle, ((AppCompatActivity) context).getSupportFragmentManager());
        waitingForDataRegistry.waitForData(formIndex);
    }

    public static BigDecimal getRangePickerValue(BigDecimal rangeStart, BigDecimal rangeStep,
                                          BigDecimal rangeEnd, Integer value) {
        BigDecimal actualValue;
        int elementCount = rangeEnd.subtract(rangeStart).abs().divide(rangeStep).intValue();
        BigDecimal multiply = new BigDecimal(elementCount - value).multiply(rangeStep);

        if (rangeStart.compareTo(rangeEnd) < 0) {
            actualValue = rangeStart.add(multiply);
        } else {
            actualValue = rangeStart.subtract(multiply);
        }
        return actualValue;
    }

    public static boolean isWidgetValid(RangeQuestion rangeQuestion) {
        BigDecimal rangeStart = rangeQuestion.getRangeStart();
        BigDecimal rangeEnd = rangeQuestion.getRangeEnd();
        BigDecimal rangeStep = rangeQuestion.getRangeStep().abs();

        boolean result = true;
        if (rangeStep.compareTo(BigDecimal.ZERO) == 0
                || rangeEnd.subtract(rangeStart).abs().remainder(rangeStep).compareTo(BigDecimal.ZERO) != 0) {
            ToastUtils.showLongToast(R.string.invalid_range_widget);
            result = false;
        }
        return result;
    }

    public static Boolean hasNoTicksAppearance(String appearance) {
        return appearance != null && appearance.contains(NO_TICKS_APPEARANCE);
    }
}