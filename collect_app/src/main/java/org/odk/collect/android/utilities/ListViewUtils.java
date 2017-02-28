package org.odk.collect.android.utilities;

import android.util.SparseBooleanArray;
import android.widget.Button;
import android.widget.ListView;

import org.odk.collect.android.R;

/**
 * Utilities for checking/unchecking list views.
 *
 * @author Jeff Wishnie (jeff@wishnie.org)
 */

public final class ListViewUtils {

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
        ListViewUtils.setAllToCheckedState(lv, newCheckState);
        return newCheckState;
    }

    public static void checkAll(ListView lv) {
        ListViewUtils.setAllToCheckedState(lv, true);
    }

    public static void uncheckAll(ListView lv) {
        ListViewUtils.setAllToCheckedState(lv, false);
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
}
