package org.odk.collect.android.fragments.dialogs;

import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.adapters.SelectMultipleListAdapter;

public class SelectMultiMinimalDialog extends SelectMinimalDialog {
    private SelectMultipleListAdapter adapter;

    public SelectMultiMinimalDialog() {
    }

    public SelectMultiMinimalDialog(SelectMultipleListAdapter adapter, boolean isFlex, boolean isAutoComplete) {
        super(isFlex, isAutoComplete);
        this.adapter = adapter;
    }

    @Override
    protected AbstractSelectListAdapter getAdapter() {
        return adapter;
    }
}
