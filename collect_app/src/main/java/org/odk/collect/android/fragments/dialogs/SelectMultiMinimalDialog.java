package org.odk.collect.android.fragments.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.adapters.SelectMultipleListAdapter;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.listeners.SelectItemClickListener;

import java.util.List;

public class SelectMultiMinimalDialog extends SelectMinimalDialog implements SelectItemClickListener {
    private SelectMultipleListAdapter adapter;

    public SelectMultiMinimalDialog() {
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public SelectMultiMinimalDialog(List<SelectChoice> items, List<Selection> selectedItems,
                                    FormEntryPrompt formEntryPrompt, ReferenceManager referenceManager,
                                    AudioHelper audioHelper, int playColor,
                                    Context context, boolean isFlex, boolean isAutoComplete) {
        super(isFlex, isAutoComplete);
        adapter = new SelectMultipleListAdapter(items, selectedItems, this, formEntryPrompt, referenceManager, audioHelper, playColor, context);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // update the listener on dialog recreation (screen rotation for example)
        ((SelectMultipleListAdapter) viewModel.getSelectListAdapter()).setSelectItemClickListener(this);
    }

    @Override
    protected AbstractSelectListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onItemClicked() {
        // do nothing
    }
}
