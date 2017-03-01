package org.odk.collect.android.activities;

import android.app.ListActivity;
import android.util.Log;
import android.widget.ListView;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ActivityLogger;

import java.util.Arrays;

abstract class AppListActivity extends ListActivity {
    private final String t = getClass().getSimpleName();
    protected final ActivityLogger logger = Collect.getInstance().getActivityLogger();

    protected boolean areCheckedItems() {
        return getListView().getCheckedItemCount() > 0;
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

    /** Checks the items at the positions contained in checkedPositions */
    protected void checkItemsAtPositions(ListView lv, int[] checkedPositions) {
        if (checkedPositions != null) {
            CheckedItemInfo cii = getCheckedItemInfo(lv);
            if (! Arrays.equals(cii.positions, checkedPositions)) {
                Log.d(t, "Setting checkboxes");
                for (int pos : checkedPositions) {
                    lv.setItemChecked(pos, true);
                }
            } else {
                Log.d(t, "No checkbox changes needed");
            }
        }
    }

    protected int getCheckedCount() {
        return getListView().getCheckedItemCount();
    }
}
