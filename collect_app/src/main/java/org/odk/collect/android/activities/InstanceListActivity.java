package org.odk.collect.android.activities;

import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

public abstract class InstanceListActivity extends AppListActivity {
    @Override
    protected void sortByNameAsc() {
        setupAdapter(InstanceColumns.DISPLAY_NAME + " ASC, " + InstanceColumns.STATUS + " DESC");
    }

    @Override
    protected void sortByNameDesc() {
        setupAdapter(InstanceColumns.DISPLAY_NAME + " DESC, " + InstanceColumns.STATUS + " DESC");
    }

    @Override
    protected void sortByDateAsc() {
        setupAdapter(InstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC");
    }

    @Override
    protected void sortByDateDesc() {
        setupAdapter(InstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC");
    }

    @Override
    protected void sortByStatusAsc() {
        setupAdapter(InstanceColumns.STATUS + " ASC, " + InstanceColumns.DISPLAY_NAME + " ASC");
    }

    @Override
    protected void sortByStatusDesc() {
        setupAdapter(InstanceColumns.STATUS + " DESC, " + InstanceColumns.DISPLAY_NAME + " ASC");
    }

    @Override
    protected void setupAdapter(String sortOrder) {
    }
}