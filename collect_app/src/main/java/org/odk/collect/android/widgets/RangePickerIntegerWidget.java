package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;

import org.odk.collect.android.R;
import org.odk.collect.android.databinding.RangePickerWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.interfaces.RangeWidgetDataRequester;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.utilities.RangeWidgetUtils;

import java.math.BigDecimal;

@SuppressLint("ViewConstructor")
public class RangePickerIntegerWidget extends QuestionWidget implements WidgetDataReceiver {
    RangePickerWidgetAnswerBinding binding;

    private String[] displayedValuesForNumberPicker;
    private BigDecimal rangeStart;
    private BigDecimal rangeEnd;
    private BigDecimal rangeStep;
    private int progress;

    private final RangeWidgetDataRequester rangeWidgetDataRequester;

    public RangePickerIntegerWidget(Context context, QuestionDetails questionDetails,
                                    RangeWidgetDataRequester rangeWidgetDataRequester) {
        super(context, questionDetails);
        this.rangeWidgetDataRequester = rangeWidgetDataRequester;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = RangePickerWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        binding.widgetAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.widgetButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        if (prompt.isReadOnly()) {
            binding.widgetButton.setVisibility(View.GONE);
        } else if (!RangeWidgetUtils.isWidgetValid((RangeQuestion) prompt.getQuestion())) {
            binding.widgetButton.setEnabled(false);
        } else {
            setUpWidgetParameters((RangeQuestion) prompt.getQuestion());
            binding.widgetButton.setOnClickListener(v ->
                    rangeWidgetDataRequester.requestRangePickerValue(prompt.getIndex(), displayedValuesForNumberPicker, progress)
            );
            setUpWidgetAnswer(context, prompt);
        }

        return binding.getRoot();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener longClickListener) {
        binding.widgetButton.setOnLongClickListener(longClickListener);
        binding.widgetAnswerText.setOnLongClickListener(longClickListener);
    }

    @Override
    public IAnswerData getAnswer() {
        return binding.widgetAnswerText.getText().toString().equals(getContext().getString(R.string.no_value_selected))
                ? null
                : new IntegerData(Integer.parseInt(binding.widgetAnswerText.getText().toString()));
    }

    @Override
    public void clearAnswer() {
        progress = 0;
        binding.widgetAnswerText.setText(getContext().getString(R.string.no_value_selected));
        binding.widgetButton.setText(getContext().getString(R.string.select_value));
        widgetValueChanged();
    }

    @Override
    public void setData(Object answer) {
        if (answer instanceof Integer) {
            BigDecimal actualValue = RangeWidgetUtils.getRangePickerValue(rangeStart, rangeStep, rangeEnd, (Integer) answer);
            progress = (actualValue.subtract(rangeStart)).divide(rangeStep).intValue();

            binding.widgetAnswerText.setText(String.valueOf(actualValue));
            binding.widgetButton.setText(R.string.edit_value);
            widgetValueChanged();
        }
    }

    private void setUpWidgetParameters(RangeQuestion rangeQuestion) {
        if (rangeQuestion.getRangeEnd().compareTo(rangeQuestion.getRangeStart()) > -1) {
            rangeStart = rangeQuestion.getRangeStart();
            rangeEnd = rangeQuestion.getRangeEnd();
        } else {
            rangeEnd = rangeQuestion.getRangeStart();
            rangeStart = rangeQuestion.getRangeEnd();
        }
        rangeStep = rangeQuestion.getRangeStep() == null ? BigDecimal.ONE : rangeQuestion.getRangeStep().abs();

        displayedValuesForNumberPicker = RangeWidgetUtils.getDisplayedValuesForNumberPicker(
                rangeStart, rangeStep, rangeEnd, true);
    }

    private void setUpWidgetAnswer(Context context, FormEntryPrompt prompt) {
        if (prompt.getAnswerText() != null) {
            BigDecimal actualValue = new BigDecimal(prompt.getAnswerText());
            progress = (actualValue.subtract(rangeStart)).divide(rangeStep).intValue();

            binding.widgetAnswerText.setText(String.valueOf(actualValue));
            binding.widgetButton.setText(context.getString(R.string.edit_value));
        }
    }
}
