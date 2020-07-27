package org.odk.collect.android.widgets;

import android.content.Context;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.SelectMinimalDialog;
import org.odk.collect.android.fragments.dialogs.SelectOneMinimalDialog;
import org.odk.collect.android.utilities.FormEntryPromptUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;

import java.util.List;

import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayColor;

public class SelectOneMinimalWidget extends SelectMinimalWidget {
    private Selection selectedItem;

    public SelectOneMinimalWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
        selectedItem = getQuestionDetails().getPrompt().getAnswerValue() == null
                ? null
                : ((Selection) getQuestionDetails().getPrompt().getAnswerValue().getValue());
        updateAnswer();
    }

    @Override
    protected void showDialog() {
        SelectOneMinimalDialog dialog = new SelectOneMinimalDialog(items, getSavedSelectedValue(), getFormEntryPrompt(), getReferenceManager(), getAnswerFontSize(), getAudioHelper(), getPlayColor(getFormEntryPrompt(), themeUtils), WidgetAppearanceUtils.isFlexAppearance(getFormEntryPrompt()), WidgetAppearanceUtils.isAutocomplete(getFormEntryPrompt()));
        dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), SelectMinimalDialog.class.getName());
    }

    @Override
    public IAnswerData getAnswer() {
        return selectedItem == null
                ? null
                : new SelectOneData(selectedItem);
    }

    @Override
    public void clearAnswer() {
        selectedItem = null;
        super.clearAnswer();
    }

    @Override
    public void setBinaryData(Object answer) {
        if (answer != null) {
            selectedItem = ((List<Selection>) answer).get(0);
            updateAnswer();
        }
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        selectedItem = isSelected
                ? items.get(choiceIndex).selection()
                : null;
    }

    private void updateAnswer() {
        if (selectedItem == null) {
            binding.choicesSearchBox.setText(R.string.select_answer);
        } else {
            binding.choicesSearchBox.setText(FormEntryPromptUtils.getItemText(getFormEntryPrompt(), selectedItem));
        }
    }

    private String getSavedSelectedValue() {
        return selectedItem == null
                ? null
                : selectedItem.getValue();
    }
}
