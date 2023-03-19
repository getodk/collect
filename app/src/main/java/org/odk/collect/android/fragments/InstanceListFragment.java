/*
Copyright 2017 Shobhit
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.odk.collect.android.fragments;

import org.odk.collect.android.database.instances.DatabaseInstanceColumns;

import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_DESC;

public abstract class InstanceListFragment extends FileManagerFragment {
    protected String getSortingOrder() {
        String sortOrder = DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + DatabaseInstanceColumns.STATUS + " DESC";
        switch (getSelectedSortingOrder()) {
            case BY_NAME_ASC:
                sortOrder = DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC, " + DatabaseInstanceColumns.STATUS + " DESC";
                break;
            case BY_NAME_DESC:
                sortOrder = DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE DESC, " + DatabaseInstanceColumns.STATUS + " DESC";
                break;
            case BY_DATE_ASC:
                sortOrder = DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE + " ASC";
                break;
            case BY_DATE_DESC:
                sortOrder = DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE + " DESC";
                break;
            case BY_STATUS_ASC:
                sortOrder = DatabaseInstanceColumns.STATUS + " ASC, " + DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
                break;
            case BY_STATUS_DESC:
                sortOrder = DatabaseInstanceColumns.STATUS + " DESC, " + DatabaseInstanceColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
                break;
        }
        return sortOrder;
    }
}
