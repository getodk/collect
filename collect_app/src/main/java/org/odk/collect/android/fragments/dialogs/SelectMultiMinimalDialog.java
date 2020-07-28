package org.odk.collect.android.fragments.dialogs;

import android.content.Context;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.adapters.SelectMultipleListAdapter;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.listeners.ItemClickListener;

import java.util.List;

public class SelectMultiMinimalDialog extends SelectMinimalDialog implements ItemClickListener {
    private SelectMultipleListAdapter adapter;

    public SelectMultiMinimalDialog() {
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public SelectMultiMinimalDialog(List<SelectChoice> items, List<Selection> selectedItems,
                                    FormEntryPrompt formEntryPrompt, ReferenceManager referenceManager,
                                    int answerFontSize, AudioHelper audioHelper, int playColor,
                                    Context context, boolean isFlex, boolean isAutoComplete) {
        super(isFlex, isAutoComplete);
        adapter = new SelectMultipleListAdapter(items, selectedItems, this, formEntryPrompt, referenceManager, answerFontSize, audioHelper, playColor, context);
    }

    @Override
    protected AbstractSelectListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onItemClicked() {
        // do nothing
    }

    @Override
    public void onStateChanged() {
        // do nothing
    }
}
