package org.odk.collect.android.fragments.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.adapters.SelectMultipleListAdapter;
import org.odk.collect.android.audio.AudioHelper;

import java.util.List;

public class SelectMultiMinimalDialog extends SelectMinimalDialog {
    public SelectMultiMinimalDialog() {
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public SelectMultiMinimalDialog(List<Selection> selectedItems, boolean isFlex, boolean isAutoComplete, Context context,
                                    List<SelectChoice> items, FormEntryPrompt prompt, ReferenceManager referenceManager,
                                    AudioHelper audioHelper, int playColor, int numColumns, boolean noButtonsMode) {
        super(isFlex, isAutoComplete);
        adapter = new SelectMultipleListAdapter(selectedItems, null, context, items, prompt,
                referenceManager, audioHelper, playColor, numColumns, noButtonsMode);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // updates needed to handle recreation (screen rotation for example)
        viewModel.getSelectListAdapter().setContext(getActivity());
        viewModel.getSelectListAdapter().setAudioHelper(audioHelperFactory.create(getActivity()));
    }
}
