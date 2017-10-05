package org.odk.collect.android.activities;

import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_DESC;

abstract class InstanceListActivity extends AppListActivity {
    protected String getSortingOrder() {
        String sortingOrder = InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + InstanceColumns.STATUS + " DESC";
        switch (getSelectedSortingOrder()) {
            case BY_NAME_ASC:
                sortingOrder = InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + InstanceColumns.STATUS + " DESC";
                break;
            case BY_NAME_DESC:
                sortingOrder = InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + InstanceColumns.STATUS + " DESC";
                break;
            case BY_DATE_ASC:
                sortingOrder = InstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC";
                break;
            case BY_DATE_DESC:
                sortingOrder = InstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC";
                break;
            case BY_STATUS_ASC:
                sortingOrder = InstanceColumns.STATUS + " ASC, " + InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
                break;
            case BY_STATUS_DESC:
                sortingOrder = InstanceColumns.STATUS + " DESC, " + InstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
                break;
        }
        return sortingOrder;
    }
}