package org.odk.collect.android.fragments.dialogs;

import android.os.Bundle;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.adapters.SelectOneListAdapter;

public class SelectOneMinimalDialog extends SelectMinimalDialog {
    private SelectOneListAdapter adapter;

    public SelectOneMinimalDialog() {
    }

    public SelectOneMinimalDialog(SelectOneListAdapter adapter, boolean isFlex, boolean isAutoComplete) {
        super(isFlex, isAutoComplete);
        this.adapter = adapter;
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((SelectOneListAdapter) viewModel.getSelectListAdapter()).setOnItemClickListener(() -> {
            viewModel.getSelectListAdapter().getFilter().filter("");
            listener.updateSelectedItems(viewModel.getSelectListAdapter().getSelectedItems());
            dismiss();
        });
    }

    @Override
    protected AbstractSelectListAdapter getAdapter() {
        return adapter;
    }
}
