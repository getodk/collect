package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.javarosa.core.model.RangeQuestion;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.databinding.RangeWidgetVerticalBinding;
import org.odk.collect.android.databinding.WidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.NumberPickerDialog;

import java.math.BigDecimal;

import timber.log.Timber;

public class RangePickerIntegerWidget extends QuestionWidget  {

    WidgetAnswerBinding binding;

    protected BigDecimal rangeStart;
    protected BigDecimal rangeEnd;
    protected BigDecimal rangeStep;
    protected BigDecimal actualValue;

    private int progress;

    public RangePickerIntegerWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
        setUpWidgetParameters();
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = WidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        View answerView = binding.getRoot();

        if (getFormEntryPrompt().getAnswerValue() != null) {
            actualValue = new BigDecimal(getFormEntryPrompt().getAnswerValue().getValue().toString());
            progress = actualValue.subtract(rangeStart).abs().divide(rangeStep).intValue();

            binding.widgetAnswerText.setText(String.valueOf(actualValue));
        } else {
            setUpNullValue();
        }

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
        return null;
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
    }
}
