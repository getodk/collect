package org.odk.collect.android.formmanagement;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.fragment.app.FragmentActivity;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.MenuDelegate;

public class BlankFormListMenuDelegate implements MenuDelegate {

    private final BlankFormsListViewModel blankFormsListViewModel;

    private Boolean outOfSync;
    private Boolean syncing;

    public BlankFormListMenuDelegate(FragmentActivity activity, BlankFormsListViewModel blankFormsListViewModel) {
        this.blankFormsListViewModel = blankFormsListViewModel;

        blankFormsListViewModel.isSyncing().observe(activity, syncing -> {
            this.syncing = syncing;
            activity.invalidateOptionsMenu();
        });

        blankFormsListViewModel.isOutOfSync().observe(activity, outOfSync -> {
            this.outOfSync = outOfSync;
            activity.invalidateOptionsMenu();
        });
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

        refreshItem.setVisible(blankFormsListViewModel.isMatchExactlyEnabled());
        refreshItem.setEnabled(!syncing);

        if (outOfSync) {
            refreshItem.setIcon(R.drawable.ic_baseline_refresh_error_24);
        } else {
            refreshItem.setIcon(R.drawable.ic_baseline_refresh_24);
        }
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
