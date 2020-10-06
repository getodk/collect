package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.databinding.RangeWidgetVerticalBinding;
import org.odk.collect.android.databinding.WidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;

public class RangePickerIntegerWidget extends QuestionWidget {

    WidgetAnswerBinding binding;

    public RangePickerIntegerWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = WidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        View answerView = binding.getRoot();

        return answerView;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    @Override
    public IAnswerData getAnswer() {
        return null;
    }

    @Override
    public void clearAnswer() {

    }
}
