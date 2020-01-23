package org.odk.collect.android.activities;

import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_PROJECT_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_PROJECT_DESC;

abstract class SmapHistoryListActivity extends AppListActivity {
    protected String getSortingOrder() {
        return InstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC";
    }
}