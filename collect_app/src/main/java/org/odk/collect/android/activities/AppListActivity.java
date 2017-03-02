package org.odk.collect.android.activities;

import android.app.ListActivity;
import android.support.annotation.NonNull;
import android.widget.ListView;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ActivityLogger;

abstract class AppListActivity extends ListActivity {
    private final String t = getClass().getSimpleName();
    protected final ActivityLogger logger = Collect.getInstance().getActivityLogger();

    protected boolean areCheckedItems() {
        return getCheckedCount() > 0;
    }

    /** The positions and IDs of the checked items */
    class CheckedItemInfo {
        final int[] positions;
        final long[] ids;

        CheckedItemInfo(int[] positions, long[] ids) {
            this.positions = positions;
            this.ids = ids;
        }
    }

    /** Returns the positions and IDs of the checked items */
    protected CheckedItemInfo getCheckedItemInfo() {
        return getCheckedItemInfo(getListView());
    }

    /** Returns the positions and IDs of the checked items */
    protected CheckedItemInfo getCheckedItemInfo(ListView lv) {
        int itemCount = lv.getCount();
        int checkedItemCount = lv.getCheckedItemCount();
        int[] checkedPositions = new int[checkedItemCount];
        long[] checkedIds = new long[checkedItemCount];
        int resultIndex = 0;
        for (int posIdx = 0; posIdx < itemCount; ++posIdx) {
            if (lv.isItemChecked(posIdx)) {
                checkedPositions[resultIndex] = posIdx;
                checkedIds      [resultIndex] = lv.getItemIdAtPosition(posIdx);
                resultIndex++;
            }
        }
        return new CheckedItemInfo(checkedPositions, checkedIds);
    }

    /** Returns the IDs of the checked items, as an array of Long */
    @NonNull
    protected Long[] getCheckedIdObjects() {
        long[] checkedIds = getCheckedItemInfo().ids;
        Long[] checkedIdObjects = new Long[checkedIds.length];
        for (int i = 0; i < checkedIds.length; ++i) {
            checkedIdObjects[i] = checkedIds[i];
        }
        return checkedIdObjects;
    }

    protected int getCheckedCount() {
        return getListView().getCheckedItemCount();
    }
}
