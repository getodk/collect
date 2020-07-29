package org.odk.collect.android.fragments.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.adapters.SelectOneListAdapter;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.listeners.SelectOneItemClickListener;

import java.util.List;

public class SelectOneMinimalDialog extends SelectMinimalDialog implements SelectOneItemClickListener {
    private SelectOneListAdapter adapter;

    public SelectOneMinimalDialog() {
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public SelectOneMinimalDialog(List<SelectChoice> items, String selectedItem,
                                  FormEntryPrompt formEntryPrompt, ReferenceManager referenceManager,
                                  AudioHelper audioHelper, int playColor,
                                  Context context, boolean isFlex, boolean isAutoComplete) {
        super(isFlex, isAutoComplete);
        adapter = new SelectOneListAdapter(items, selectedItem, this, formEntryPrompt, referenceManager, audioHelper, playColor, context);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // update the listener on dialog recreation (screen rotation for example)
        ((SelectOneListAdapter) viewModel.getSelectListAdapter()).setSelectItemClickListener(this);
    }

    @Override
    protected AbstractSelectListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onItemClicked() {
        closeDialogAndSaveAnswers();
    }

    @Override
    public void onClearNextLevelsOfCascadingSelect() {
        // do nothing
    }
}
