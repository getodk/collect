package org.odk.collect.android.fragments;

import org.odk.collect.android.provider.InstanceProviderAPI;

/**
 * Created by shobhit on 12/3/17.
 */

public class InstanceListFragment extends AppListFragment{
    @Override
    protected void sortByNameAsc() {
        setupAdapter(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC, " +
                InstanceProviderAPI.InstanceColumns.STATUS + " DESC");
    }

    @Override
    protected void sortByNameDesc() {
        setupAdapter(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " DESC, " +
                InstanceProviderAPI.InstanceColumns.STATUS + " DESC");
    }

    @Override
    protected void sortByDateAsc() {
        setupAdapter(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC");
    }

    @Override
    protected void sortByDateDesc() {
        setupAdapter(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC");
    }

    @Override
    protected void sortByStatusAsc() {
        setupAdapter(InstanceProviderAPI.InstanceColumns.STATUS + " ASC, " +
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC");
    }

    @Override
    protected void sortByStatusDesc() {
        setupAdapter(InstanceProviderAPI.InstanceColumns.STATUS + " DESC, " +
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC");
    }

    @Override
    protected void setupAdapter(String sortOrder) {
    }
}
