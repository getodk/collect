package org.odk.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.databinding.SelectMinimalWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.SelectMinimalDialog;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.interfaces.BinaryWidget;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import java.util.List;

public abstract class SelectMinimalWidget extends ItemsWidget implements BinaryWidget, MultiChoiceWidget {
    SelectMinimalWidgetAnswerBinding binding;
    protected AbstractSelectListAdapter recyclerViewAdapter;

    public SelectMinimalWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = SelectMinimalWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        binding.choicesSearchBox.setTextSize(QuestionFontSizeUtils.getQuestionFontSize());
        if (prompt.isReadOnly()) {
            binding.choicesSearchBox.setEnabled(false);
        } else {
            binding.choicesSearchBox.setOnClickListener(v -> {
                FormController formController = Collect.getInstance().getFormController();
                if (formController != null) {
                    formController.setIndexWaitingForData(getFormEntryPrompt().getIndex());
                }
                SelectMinimalDialog dialog = new SelectMinimalDialog(recyclerViewAdapter, WidgetAppearanceUtils.isFlexAppearance(getFormEntryPrompt()), WidgetAppearanceUtils.isAutocomplete(getFormEntryPrompt()));
                dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), SelectMinimalDialog.class.getName());
            });
        }
        return binding.getRoot();
    }

    @Override
    public void clearAnswer() {
        recyclerViewAdapter.clearAnswer();
        binding.choicesSearchBox.setText(R.string.select_answer);
        widgetValueChanged();
    }

    @Override
    public void setBinaryData(Object answer) {
        recyclerViewAdapter.updateSelectedItems((List<Selection>) answer);
        updateAnswer();
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    public void onButtonClick(int buttonId) {
    }

    @Override
    public int getChoiceCount() {
        return recyclerViewAdapter.getItemCount();
    }

    void updateAnswer() {
        List<Selection> selectedItems = recyclerViewAdapter.getSelectedItems();
        if (selectedItems != null) {
            StringBuilder builder = new StringBuilder();
            for (Selection selectedItem : selectedItems) {
                builder.append(FormEntryPromptUtils.getItemText(getFormEntryPrompt(), selectedItem));
                if (selectedItems.size() - 1 > selectedItems.indexOf(selectedItem)) {
                    builder.append(", ");
                }
            }
            binding.choicesSearchBox.setText(builder.toString());
        }
    }
}
