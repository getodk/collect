package org.odk.collect.android.fragments;

import android.support.v4.app.ListFragment;
import android.widget.Button;
import android.widget.ListView;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ActivityLogger;


public class AppListFragment extends ListFragment {
    protected final ActivityLogger logger = Collect.getInstance().getActivityLogger();

    // toggles to all checked or all unchecked
    // returns:
    // true if result is all checked
    // false if result is all unchecked
    //
    // Toggle behavior is as follows:
    // if ANY items are unchecked, check them all
    // if ALL items are checked, uncheck them all
    public static boolean toggleChecked(ListView lv) {
        // shortcut null case
        if (lv == null) return false;

        boolean newCheckState = lv.getCount() > lv.getCheckedItemCount();
        setAllToCheckedState(lv, newCheckState);
        return newCheckState;
    }

    public static void setAllToCheckedState(ListView lv, boolean check) {
        // no-op if ListView null
        if (lv == null) return;

        for (int x = 0; x < lv.getCount(); x++) {
            lv.setItemChecked(x, check);
        }
    }

    // Function to toggle button label
    public static void toggleButtonLabel(Button mToggleButton, ListView lv) {
        if (lv.getCheckedItemCount() != lv.getCount()) {
            mToggleButton.setText(R.string.select_all);
        } else {
            mToggleButton.setText(R.string.clear_all);
        }
    }

    protected boolean areCheckedItems() {
        return getCheckedCount() > 0;
    }

    /**
     * Returns the IDs of the checked items, using the ListView of this activity.
     */
    protected long[] getCheckedIds() {
        return getCheckedIds(getListView());
    }

    /**
     * Returns the IDs of the checked items, using the ListView provided
     */
    protected long[] getCheckedIds(ListView lv) {
        // This method could be simplified by using getCheckedItemIds, if one ensured that
        // IDs were “stable” (see the getCheckedItemIds doc).
        int itemCount = lv.getCount();
        int checkedItemCount = lv.getCheckedItemCount();
        long[] checkedIds = new long[checkedItemCount];
        int resultIndex = 0;
        for (int posIdx = 0; posIdx < itemCount; posIdx++) {
            if (lv.isItemChecked(posIdx)) {
                checkedIds[resultIndex] = lv.getItemIdAtPosition(posIdx);
                resultIndex++;
            }
        }
        return checkedIds;
    }

    /**
     * Returns the IDs of the checked items, as an array of Long
     */
    protected Long[] getCheckedIdObjects() {
        long[] checkedIds = getCheckedIds();
        Long[] checkedIdObjects = new Long[checkedIds.length];
        for (int i = 0; i < checkedIds.length; i++) {
            checkedIdObjects[i] = checkedIds[i];
        }
        return checkedIdObjects;
    }

    protected int getCheckedCount() {
        return getListView().getCheckedItemCount();
    }
}
