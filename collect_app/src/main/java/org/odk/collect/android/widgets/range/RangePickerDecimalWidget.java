package org.odk.collect.android.widgets.range;

import static org.odk.collect.android.fragments.dialogs.NumberPickerDialog.ARG_FORM_INDEX;
import static org.odk.collect.android.fragments.dialogs.NumberPickerDialog.DISPLAYED_VALUES;
import static org.odk.collect.android.fragments.dialogs.NumberPickerDialog.PROGRESS;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.databinding.RangePickerWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.utilities.RangeWidgetUtils;
import org.odk.collect.androidshared.ui.DialogFragmentUtils;

import java.math.BigDecimal;

public class RangePickerDecimalWidget extends QuestionWidget {
    RangePickerWidgetAnswerBinding binding;
    private String[] displayedValuesForNumberPicker;

    private BigDecimal rangeStart;
    private BigDecimal rangeEnd;
    private BigDecimal rangeStep;

    private int progress;

    public RangePickerDecimalWidget(Context context, QuestionDetails questionDetails, Dependencies dependencies) {
        super(context, dependencies, questionDetails);
        render();
    }

    @Override
    protected View onCreateWidgetView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = RangePickerWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        binding.widgetAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        setUpWidgetParameters();
        displayedValuesForNumberPicker = RangePickerWidgetUtils.getNumbersFromRangeAsc(
                rangeStart, rangeStep, rangeEnd, false);
        RangeWidgetUtils.setUpRangePickerWidget(context, binding, prompt);
        progress = RangePickerWidgetUtils.getProgressFromPrompt(prompt, displayedValuesForNumberPicker);
        binding.widgetButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putSerializable(ARG_FORM_INDEX, prompt.getIndex());
            args.putInt(PROGRESS, progress);
            args.putSerializable(DISPLAYED_VALUES, displayedValuesForNumberPicker);
            DialogFragmentUtils.showIfNotShowing(NumberPickerDialog.class, args, ((FragmentActivity) context).getSupportFragmentManager());
        });

        return binding.getRoot();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.widgetButton.setOnLongClickListener(l);
        binding.widgetAnswerText.setOnLongClickListener(l);
    }

    @Override
    public IAnswerData getAnswer() {
        return binding.widgetAnswerText.getText().toString().equals(getContext().getString(org.odk.collect.strings.R.string.no_value_selected))
                ? null
                : new DecimalData(Double.parseDouble(binding.widgetAnswerText.getText().toString()));
    }

    @Override
    public void clearAnswer() {
        setUpNullValue();
        widgetValueChanged();
    }

    private void setUpWidgetParameters() {
        RangeQuestion rangeQuestion = (RangeQuestion) getFormEntryPrompt().getQuestion();
        rangeStart = rangeQuestion.getRangeStart();
        rangeEnd = rangeQuestion.getRangeEnd();
        rangeStep = rangeQuestion.getRangeStep().abs() != null ? rangeQuestion.getRangeStep().abs() : new BigDecimal("0.5");
    }

    private void setUpNullValue() {
        progress = 0;
        binding.widgetAnswerText.setText(getContext().getString(org.odk.collect.strings.R.string.no_value_selected));
        binding.widgetButton.setText(getContext().getString(org.odk.collect.strings.R.string.select_value));
    }
}
