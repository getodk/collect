package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.databinding.WidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.utilities.RangeWidgetUtils;

import java.math.BigDecimal;

public class RangePickerIntegerWidget extends QuestionWidget  {
    WidgetAnswerBinding binding;
    String[] displayedValuesForNumberPicker;

    private BigDecimal rangeStart;
    private BigDecimal rangeEnd;
    private BigDecimal rangeStep;
    private BigDecimal actualValue;
    private int progress;

    public RangePickerIntegerWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = WidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        View answerView = binding.getRoot();
        setUpWidgetParameters();

        actualValue = RangeWidgetUtils.setUpRangePickerWidget(context, binding, prompt);
        if (actualValue != null) {
            progress = actualValue.subtract(rangeStart).abs().divide(rangeStep).intValue();
        } else {
            progress = 0;
        }
        displayedValuesForNumberPicker = RangeWidgetUtils.setUpDisplayedValuesForNumberPicker(rangeStart, rangeStep, rangeEnd, true);

        binding.widgetAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.widgetButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.widgetButton.setOnClickListener(v -> onButtonClick());
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
        actualValue = RangeWidgetUtils.setUpNullValueForRangePicker(binding);
    }

    private void onButtonClick() {
        RangeWidgetUtils.showNumberPickerDialog((FormEntryActivity) getContext(), displayedValuesForNumberPicker, getId(), progress);
    }

    public void setNumberPickerValue(int value) {
        progress = RangeWidgetUtils.getNumberPickerProgress(binding, rangeStart, rangeStep, rangeEnd, value);
        actualValue = new BigDecimal((String) binding.widgetAnswerText.getText());
    }
}
