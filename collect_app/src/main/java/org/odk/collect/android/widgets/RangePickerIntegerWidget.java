package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.databinding.RangeWidgetVerticalBinding;
import org.odk.collect.android.databinding.WidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;
import org.odk.collect.android.utilities.ToastUtils;

import java.math.BigDecimal;

import timber.log.Timber;

public class RangePickerIntegerWidget extends QuestionWidget  {

    WidgetAnswerBinding binding;
    String[] displayedValuesForNumberPicker;

    private BigDecimal rangeStart;
    private BigDecimal rangeEnd;
    private BigDecimal rangeStep;
    private BigDecimal actualValue;
    private int elementCount;
    private int progress;

    public RangePickerIntegerWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = WidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        View answerView = binding.getRoot();

        setUpWidgetParameters();
        if (isWidgetValid()) {
            elementCount = rangeEnd.subtract(rangeStart).abs().divide(rangeStep).intValue();
            if (getFormEntryPrompt().getAnswerValue() != null) {
                actualValue = new BigDecimal(getFormEntryPrompt().getAnswerValue().getValue().toString());
                progress = actualValue.subtract(rangeStart).abs().divide(rangeStep).intValue();

                binding.widgetAnswerText.setText(String.valueOf(actualValue));
            } else {
                setUpNullValue();
            }
            setUpDisplayedValuesForNumberPicker();
        }

        binding.widgetAnswerText.setText(getFormEntryPrompt().getAnswerValue() != null ? String.valueOf(actualValue) : getContext().getString(R.string.no_value_selected));
        binding.widgetButton.setText(getFormEntryPrompt().getAnswerValue() != null ? getContext().getString(R.string.edit_value) : getContext().getString(R.string.select_value));
        binding.widgetButton.setOnClickListener(v -> onButtonClick());

        if (prompt.isReadOnly()) {
            binding.widgetButton.setEnabled(false);
        }

        return answerView;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.widgetButton.setOnLongClickListener(l);
        binding.widgetAnswerText.setOnLongClickListener(l);
    }

    @Override
    public IAnswerData getAnswer() {
        return actualValue != null
                ? new IntegerData(actualValue.intValue())
                : null;
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
        rangeStep = rangeQuestion.getRangeStep().abs();
    }

    private void setUpNullValue() {
        progress = 0;
        actualValue = null;
        binding.widgetAnswerText.setText(R.string.no_value_selected);
        binding.widgetButton.setText(R.string.select_value);
    }

    private void onButtonClick() {
        showNumberPickerDialog();
    }

    private void disableWidget() {
        ToastUtils.showLongToast(R.string.invalid_range_widget);
        binding.widgetButton.setEnabled(false);
    }

    private boolean isWidgetValid() {
        boolean result = true;
        if (rangeStep.compareTo(BigDecimal.ZERO) == 0
                || rangeEnd.subtract(rangeStart).remainder(rangeStep).compareTo(BigDecimal.ZERO) != 0) {
            disableWidget();
            result = false;
        }
        return result;
    }

    public void setNumberPickerValue(int value) {
        if (rangeStart.compareTo(rangeEnd) == -1) {
            actualValue = rangeStart.add(new BigDecimal(elementCount - value).multiply(rangeStep));
        } else {
            actualValue = rangeStart.subtract(new BigDecimal(elementCount - value).multiply(rangeStep));
        }

        progress = actualValue.subtract(rangeStart).abs().divide(rangeStep).intValue();

        binding.widgetAnswerText.setText(String.valueOf(actualValue));
        binding.widgetButton.setText(R.string.edit_value);
    }

    private void showNumberPickerDialog() {
        NumberPickerDialog dialog = NumberPickerDialog.newInstance(getId(), displayedValuesForNumberPicker, progress);

        try {
            dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), NumberPickerDialog.NUMBER_PICKER_DIALOG_TAG);
        } catch (ClassCastException e) {
            Timber.i(e);
        }
    }

    private void setUpDisplayedValuesForNumberPicker() {
        int index = 0;
        displayedValuesForNumberPicker = new String[elementCount +1];

        if (rangeEnd.compareTo(rangeStart) > -1) {
            for (int i = rangeEnd.intValue(); i >= rangeStart.intValue(); i -= rangeStep.abs().intValue()) {
                displayedValuesForNumberPicker[index] = String.valueOf(i);
                index++;
            }
        } else {
            for (int i = rangeEnd.intValue(); i <= rangeStart.intValue(); i += rangeStep.abs().intValue()) {
                displayedValuesForNumberPicker[index] = String.valueOf(i);
                index++;
            }
        }
    }
}
