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

import java.util.List;

public class SelectOneMinimalDialog extends SelectMinimalDialog {
    private SelectOneListAdapter adapter;

    public SelectOneMinimalDialog() {
    }

    public SelectOneMinimalDialog(List<SelectChoice> items, String selectedItem,
                                  FormEntryPrompt formEntryPrompt, ReferenceManager referenceManager,
                                  int answerFontSize, AudioHelper audioHelper, int playColor,
                                  Context context, boolean isFlex, boolean isAutoComplete) {
        super(isFlex, isAutoComplete);
        this.adapter = new SelectOneListAdapter(items, selectedItem, null, formEntryPrompt, referenceManager, answerFontSize, audioHelper, playColor, context);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((SelectOneListAdapter) viewModel.getSelectListAdapter()).setOnItemClickListener(this::closeDialogAndSaveAnswers);
    }

    @Override
    protected AbstractSelectListAdapter getAdapter() {
        return adapter;
    }
}
