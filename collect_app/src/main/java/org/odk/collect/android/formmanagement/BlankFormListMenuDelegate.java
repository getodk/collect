package org.odk.collect.android.formmanagement;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.MenuDelegate;

public class BlankFormListMenuDelegate implements MenuDelegate {

    private final BlankFormsListViewModel blankFormsListViewModel;

    public BlankFormListMenuDelegate(BlankFormsListViewModel blankFormsListViewModel) {
        this.blankFormsListViewModel = blankFormsListViewModel;
    }

    @Override
    public void onCreateOptionsMenu(MenuInflater menuInflater, Menu menu) {
        // FormChooserListActivity uses the list inflated in AppListActivityMenu. It probably
        // makes sense to decouple this if we can so each Activity can have control over
        // its own menu layout
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem refreshItem = menu.findItem(R.id.menu_refresh);

        refreshItem.setVisible(blankFormsListViewModel.isSyncingAvailable());
        refreshItem.setEnabled(!blankFormsListViewModel.isSyncing().getValue());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            blankFormsListViewModel.syncWithServer();
            return true;
        } else {
            return false;
        }
    }
}
