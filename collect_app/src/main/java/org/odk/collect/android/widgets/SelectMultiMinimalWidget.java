package org.odk.collect.android.widgets;

import android.content.Context;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.adapters.SelectMultipleListAdapter;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.SelectMinimalDialog;
import org.odk.collect.android.fragments.dialogs.SelectMultiMinimalDialog;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayColor;

public class SelectMultiMinimalWidget extends SelectMinimalWidget {
    private final SelectMultipleListAdapter adapter;

    public SelectMultiMinimalWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
        adapter = new SelectMultipleListAdapter(items, getSavedSelectedItems(), null, getFormEntryPrompt(), getReferenceManager(), getAnswerFontSize(), getAudioHelper(), getPlayColor(getFormEntryPrompt(), themeUtils), getContext());
        updateAnswer();
    }

    @Override
    protected void showDialog() {
        SelectMultiMinimalDialog dialog = new SelectMultiMinimalDialog(adapter, WidgetAppearanceUtils.isFlexAppearance(getFormEntryPrompt()), WidgetAppearanceUtils.isAutocomplete(getFormEntryPrompt()));
        dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), SelectMinimalDialog.class.getName());
    }

    @Override
    protected AbstractSelectListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public IAnswerData getAnswer() {
        List<Selection> selectedItems = adapter.getSelectedItems();
        return selectedItems.isEmpty()
                ? null
                : new SelectMultiData(selectedItems);
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        if (isSelected) {
            adapter.addItem(items.get(choiceIndex).selection());
        } else {
            adapter.removeItem(items.get(choiceIndex).selection());
        }
    }

    private List<Selection> getSavedSelectedItems() {
        return getFormEntryPrompt().getAnswerValue() == null
                ? new ArrayList<>() :
                (List<Selection>) getFormEntryPrompt().getAnswerValue().getValue();
    }
}
