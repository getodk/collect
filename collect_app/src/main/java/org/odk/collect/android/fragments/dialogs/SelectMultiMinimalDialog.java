package org.odk.collect.android.fragments.dialogs;

import android.os.Bundle;
import android.view.View;

import org.javarosa.core.model.data.helper.Selection;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.adapters.SelectMultipleListAdapter;
import org.odk.collect.android.listeners.SelectItemClickListener;
import org.odk.collect.android.logic.ChoicesRecyclerViewAdapterProps;

import java.util.List;

public class SelectMultiMinimalDialog extends SelectMinimalDialog implements SelectItemClickListener {
    private SelectMultipleListAdapter adapter;

    public SelectMultiMinimalDialog() {
    }

    public SelectMultiMinimalDialog(List<Selection> selectedItems, boolean isFlex, boolean isAutoComplete, ChoicesRecyclerViewAdapterProps props) {
        super(isFlex, isAutoComplete);
        adapter = new SelectMultipleListAdapter(selectedItems, this, props);
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
