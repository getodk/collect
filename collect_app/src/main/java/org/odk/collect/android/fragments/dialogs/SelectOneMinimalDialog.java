package org.odk.collect.android.fragments.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.adapters.SelectOneListAdapter;
import org.odk.collect.android.listeners.SelectItemClickListener;

import java.util.List;

public class SelectOneMinimalDialog extends SelectMinimalDialog implements SelectItemClickListener {
    public SelectOneMinimalDialog() {
    }

    public SelectOneMinimalDialog(String selectedItem, boolean isFlex, boolean isAutoComplete, Context context,
                                  List<SelectChoice> items, FormEntryPrompt prompt, ReferenceManager referenceManager,
                                  int playColor, int numColumns, boolean noButtonsMode) {
        super(isFlex, isAutoComplete);
        adapter = new SelectOneListAdapter(selectedItem, this, context, items, prompt,
                referenceManager, null, playColor, numColumns, noButtonsMode);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // updates needed to handle recreation (screen rotation for example)
        ((SelectOneListAdapter) viewModel.getSelectListAdapter()).setSelectItemClickListener(this);
    }

    @Override
    public void onItemClicked() {
        closeDialogAndSaveAnswers();
    }
}
