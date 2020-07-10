package org.odk.collect.android.widgets;

import android.content.Context;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.adapters.SelectMultipleListAdapter;
import org.odk.collect.android.formentry.questions.QuestionDetails;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayColor;

public class SelectMultiMinimalWidget extends SelectMinimalWidget {
    public SelectMultiMinimalWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
        recyclerViewAdapter = new SelectMultipleListAdapter(items, getSavedSelectedItems(), null, getFormEntryPrompt(), getReferenceManager(), getAnswerFontSize(), getAudioHelper(), getPlayColor(getFormEntryPrompt(), themeUtils), getContext());
        updateAnswer();
    }

    @Override
    public IAnswerData getAnswer() {
        List<Selection> selectedItems = recyclerViewAdapter.getSelectedItems();
        return selectedItems.isEmpty()
                ? null
                : new SelectMultiData(selectedItems);
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        if (isSelected) {
            ((SelectMultipleListAdapter) recyclerViewAdapter).addItem(items.get(choiceIndex).selection());
        } else {
            ((SelectMultipleListAdapter) recyclerViewAdapter).removeItem(items.get(choiceIndex).selection());
        }
    }

    private List<Selection> getSavedSelectedItems() {
        return getFormEntryPrompt().getAnswerValue() == null
                ? new ArrayList<>() :
                (List<Selection>) getFormEntryPrompt().getAnswerValue().getValue();
    }
}
