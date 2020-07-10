package org.odk.collect.android.widgets;

import android.content.Context;
import android.widget.RadioButton;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.adapters.SelectOneListAdapter;
import org.odk.collect.android.formentry.questions.QuestionDetails;

import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayColor;

public class SelectOneMinimalWidget extends SelectMinimalWidget {

    public SelectOneMinimalWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
        recyclerViewAdapter = new SelectOneListAdapter(items, getSavedSelectedValue(), null, getFormEntryPrompt(), getReferenceManager(), getAnswerFontSize(), getAudioHelper(), getPlayColor(getFormEntryPrompt(), themeUtils), getContext());
        updateAnswer();
    }

    @Override
    public IAnswerData getAnswer() {
        Selection selectedItem = ((SelectOneListAdapter) recyclerViewAdapter).getSelectedItem();
        return selectedItem == null
                ? null
                : new SelectOneData(selectedItem);
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        RadioButton button = new RadioButton(getContext());
        button.setTag(choiceIndex);
        button.setChecked(isSelected);

        ((SelectOneListAdapter) recyclerViewAdapter).onCheckedChanged(button, isSelected);
    }

    private String getSavedSelectedValue() {
        return getQuestionDetails().getPrompt().getAnswerValue() == null
                ? null
                : ((Selection) getQuestionDetails().getPrompt().getAnswerValue().getValue()).getValue();
    }
}
