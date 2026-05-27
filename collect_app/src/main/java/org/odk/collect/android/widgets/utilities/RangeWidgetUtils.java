package org.odk.collect.android.widgets.utilities;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.databinding.RangePickerWidgetAnswerBinding;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.androidshared.ui.ToastUtils;

import java.math.BigDecimal;

import timber.log.Timber;

public class RangeWidgetUtils {
    private RangeWidgetUtils() {
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
