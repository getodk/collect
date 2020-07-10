package org.odk.collect.android.widgets.utilities;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.widget.TextView;

import org.javarosa.core.model.RangeQuestion;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.views.CustomRangeSlider;

import java.math.BigDecimal;

public class RangeWidgetUtils {

    private RangeWidgetUtils() {
    }

    public static void setUpWidgetParameters(RangeQuestion rangeQuestion, TextView minValue, TextView maxValue) {
        minValue.setText(String.valueOf(rangeQuestion.getRangeStart()));
        maxValue.setText(String.valueOf(rangeQuestion.getRangeEnd()));
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void setUpSlider(RangeQuestion rangeQuestion, CustomRangeSlider slider, BigDecimal actualValue) {
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

    public static BigDecimal setUpNullValue(CustomRangeSlider slider, TextView currentValue) {
        slider.setValue(slider.getValueFrom());
        currentValue.setText("");
        return null;
    }

    public static boolean isWidgetValid(RangeQuestion rangeQuestion, CustomRangeSlider slider) {
        BigDecimal rangeStart = rangeQuestion.getRangeStart();
        BigDecimal rangeEnd = rangeQuestion.getRangeEnd();
        BigDecimal rangeStep = rangeQuestion.getRangeStep().abs();

        boolean result = true;
        if (rangeStep.compareTo(BigDecimal.ZERO) == 0
                || rangeEnd.subtract(rangeStart).remainder(rangeStep).compareTo(BigDecimal.ZERO) != 0) {
            disableWidget(slider);
            result = false;
        }
        return result;
    }

    private static void disableWidget(CustomRangeSlider slider) {
        ToastUtils.showLongToast(R.string.invalid_range_widget);
        slider.setEnabled(false);
    }
}
