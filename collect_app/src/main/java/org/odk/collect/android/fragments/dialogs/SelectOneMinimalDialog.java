package org.odk.collect.android.fragments.dialogs;

import android.os.Bundle;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.adapters.SelectOneListAdapter;
import org.odk.collect.android.listeners.SelectOneItemClickListener;
import org.odk.collect.android.logic.ChoicesRecyclerViewAdapterProps;

public class SelectOneMinimalDialog extends SelectMinimalDialog implements SelectOneItemClickListener {
    private SelectOneListAdapter adapter;

    public SelectOneMinimalDialog() {
    }

    public SelectOneMinimalDialog(String selectedItem, boolean isFlex, boolean isAutoComplete, ChoicesRecyclerViewAdapterProps props) {
        super(isFlex, isAutoComplete);
        adapter = new SelectOneListAdapter(selectedItem, this, props);
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
