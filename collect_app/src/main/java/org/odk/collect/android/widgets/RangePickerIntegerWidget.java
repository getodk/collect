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
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.databinding.RangePickerWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.utilities.RangeWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import java.math.BigDecimal;

@SuppressLint("ViewConstructor")
public class RangePickerIntegerWidget extends QuestionWidget implements WidgetDataReceiver {
    RangePickerWidgetAnswerBinding binding;

    private String[] displayedValuesForNumberPicker;
    private BigDecimal rangeStart;
    private BigDecimal rangeEnd;
    private BigDecimal rangeStep;
    private int progress;

    private final WaitingForDataRegistry waitingForDataRegistry;

    public RangePickerIntegerWidget(Context context, QuestionDetails questionDetails, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, questionDetails);
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = RangePickerWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        binding.widgetAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.widgetButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        setUpWidgetParameters();
        displayedValuesForNumberPicker = RangeWidgetUtils.getDisplayedValuesForNumberPicker(
                rangeStart, rangeStep, rangeEnd, true);
        RangeWidgetUtils.setUpRangePickerWidget(context, binding, prompt);

        progress = RangeWidgetUtils.getRangePickerProgressFromPrompt(prompt);

        binding.widgetButton.setOnClickListener(v -> {
                waitingForDataRegistry.waitForData(prompt.getIndex());
                RangeWidgetUtils.showNumberPickerDialog(
                (FormEntryActivity) getContext(), displayedValuesForNumberPicker, progress);
        });

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
            progress = RangeWidgetUtils.getNumberPickerProgress(binding,
                    rangeStart, rangeStep, rangeEnd, (Integer) answer);
        }
    }

    private void setUpWidgetParameters() {
        RangeQuestion rangeQuestion = (RangeQuestion) getFormEntryPrompt().getQuestion();
        rangeStart = rangeQuestion.getRangeStart();
        rangeEnd = rangeQuestion.getRangeEnd();
        rangeStep = rangeQuestion.getRangeStep().abs();
    }
}
