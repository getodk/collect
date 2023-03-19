package org.odk.collect.android.fragments.dialogs;

import android.content.Context;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.adapters.SelectMultipleListAdapter;
import org.odk.collect.android.utilities.MediaUtils;

import java.util.List;

public class SelectMultiMinimalDialog extends SelectMinimalDialog {
    public SelectMultiMinimalDialog() {
    }

    public SelectMultiMinimalDialog(List<Selection> selectedItems, boolean isFlex, boolean isAutoComplete, Context context,
                                    List<SelectChoice> items, FormEntryPrompt prompt, ReferenceManager referenceManager,
                                    int playColor, int numColumns, boolean noButtonsMode, MediaUtils mediaUtils) {
        super(isFlex, isAutoComplete);
        adapter = new SelectMultipleListAdapter(selectedItems, null, context, items, prompt,
                referenceManager, null, playColor, numColumns, noButtonsMode, mediaUtils);
    }
}
