package org.odk.collect.android.fragments.dialogs;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.adapters.SelectMultipleListAdapter;
import org.odk.collect.android.audio.AudioHelper;

import java.util.List;

public class SelectMultiMinimalDialog extends SelectMinimalDialog {
    private SelectMultipleListAdapter adapter;

    public SelectMultiMinimalDialog() {
    }

    public SelectMultiMinimalDialog(List<SelectChoice> items, List<Selection> selectedItems,
                                    FormEntryPrompt formEntryPrompt, ReferenceManager referenceManager,
                                    int answerFontSize, AudioHelper audioHelper, int playColor,
                                    boolean isFlex, boolean isAutoComplete) {
        super(isFlex, isAutoComplete);
        this.adapter = new SelectMultipleListAdapter(items, selectedItems, null, formEntryPrompt, referenceManager, answerFontSize, audioHelper, playColor, getContext());
    }

    @Override
    protected AbstractSelectListAdapter getAdapter() {
        return adapter;
    }
}
