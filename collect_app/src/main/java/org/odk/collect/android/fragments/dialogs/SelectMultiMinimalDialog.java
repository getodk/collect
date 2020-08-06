package org.odk.collect.android.fragments.dialogs;

import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.adapters.SelectMultipleListAdapter;
import org.odk.collect.android.logic.ChoicesRecyclerViewAdapterProps;

import java.util.List;

public class SelectMultiMinimalDialog extends SelectMinimalDialog {
    private SelectMultipleListAdapter adapter;

    public SelectMultiMinimalDialog() {
    }

    public SelectMultiMinimalDialog(List<Selection> selectedItems, boolean isFlex, boolean isAutoComplete, ChoicesRecyclerViewAdapterProps props) {
        super(isFlex, isAutoComplete);
        adapter = new SelectMultipleListAdapter(selectedItems, null, props);
    }

    @Override
    protected AbstractSelectListAdapter getAdapter() {
        return adapter;
    }
}
