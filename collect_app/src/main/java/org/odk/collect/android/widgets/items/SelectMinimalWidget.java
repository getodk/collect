package org.odk.collect.android.widgets.items;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.databinding.SelectMinimalWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

public abstract class SelectMinimalWidget extends ItemsWidget implements WidgetDataReceiver, MultiChoiceWidget {
    SelectMinimalWidgetAnswerBinding binding;
    private final WaitingForDataRegistry waitingForDataRegistry;

    public SelectMinimalWidget(Context context, QuestionDetails prompt, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, prompt);
        this.waitingForDataRegistry = waitingForDataRegistry;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = SelectMinimalWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        binding.answer.setTextSize(QuestionFontSizeUtils.getQuestionFontSize());
        if (prompt.isReadOnly()) {
            binding.answer.setEnabled(false);
        } else {
            binding.answer.setOnClickListener(v -> {
                waitingForDataRegistry.waitForData(prompt.getIndex());
                showDialog();
            });
        }
        return binding.getRoot();
    }

    @Override
    public void clearAnswer() {
        binding.answer.setText(R.string.select_answer);
        widgetValueChanged();
    }

    @Override
    public int getChoiceCount() {
        return items.size();
    }

    protected abstract void showDialog();
}
