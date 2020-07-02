package odk.hedera.collect.activities;

import odk.hedera.collect.provider.FormsProviderAPI.FormsColumns;

import static odk.hedera.collect.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static odk.hedera.collect.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static odk.hedera.collect.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static odk.hedera.collect.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;

abstract class FormListActivity extends AppListActivity {

    protected static final String SORT_BY_NAME_ASC
            = FormsColumns.DISPLAY_NAME + " COLLATE NOCASE ASC";
    protected static final String SORT_BY_NAME_DESC
            = FormsColumns.DISPLAY_NAME + " COLLATE NOCASE DESC";
    protected static final String SORT_BY_DATE_ASC = FormsColumns.DATE + " ASC";
    protected static final String SORT_BY_DATE_DESC = FormsColumns.DATE + " DESC";

    protected String getSortingOrder() {
        String sortingOrder = SORT_BY_NAME_ASC;
        switch (getSelectedSortingOrder()) {
            case BY_NAME_ASC:
                sortingOrder = SORT_BY_NAME_ASC;
                break;
            case BY_NAME_DESC:
                sortingOrder = SORT_BY_NAME_DESC;
                break;
            case BY_DATE_ASC:
                sortingOrder = SORT_BY_DATE_ASC;
                break;
            case BY_DATE_DESC:
                sortingOrder = SORT_BY_DATE_DESC;
                break;
        }
        return sortingOrder;
    }
}
