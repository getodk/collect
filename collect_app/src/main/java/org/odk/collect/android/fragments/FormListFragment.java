package org.odk.collect.android.fragments;

import org.odk.collect.android.provider.FormsProviderAPI;

/**
 * Created by shobhit on 12/3/17.
 */

public class FormListFragment extends AppListFragment {
    @Override
    protected void sortByNameAsc() {
        setupAdapter(FormsProviderAPI.FormsColumns.DISPLAY_NAME + " ASC");
    }

    @Override
    protected void sortByNameDesc() {
        setupAdapter(FormsProviderAPI.FormsColumns.DISPLAY_NAME + " DESC");
    }

    @Override
    protected void sortByDateAsc() {
        setupAdapter(FormsProviderAPI.FormsColumns.DATE + " ASC");
    }

    @Override
    protected void sortByDateDesc() {
        setupAdapter(FormsProviderAPI.FormsColumns.DATE + " DESC");
    }

    @Override
    protected void sortByStatusAsc() {
    }

    @Override
    protected void sortByStatusDesc() {
    }

    @Override
    protected void setupAdapter(String sortOrder) {
    }
}
