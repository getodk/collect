package org.odk.collect.android.formmanagement;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import org.odk.collect.android.R;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.android.utilities.MenuDelegate;

public class BlankFormListMenuDelegate implements MenuDelegate {

    private final FragmentActivity activity;
    private final BlankFormsListViewModel blankFormsListViewModel;
    private final NetworkStateProvider networkStateProvider;

    private Boolean outOfSync;
    private Boolean syncing;

    public BlankFormListMenuDelegate(FragmentActivity activity, BlankFormsListViewModel blankFormsListViewModel, NetworkStateProvider networkStateProvider) {
        this.activity = activity;
        this.blankFormsListViewModel = blankFormsListViewModel;
        this.networkStateProvider = networkStateProvider;

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

        menu.findItem(R.id.menu_filter).setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                menu.findItem(R.id.menu_refresh).setVisible(false);
                menu.findItem(R.id.menu_sort).setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                menu.findItem(R.id.menu_refresh).setVisible(blankFormsListViewModel.isMatchExactlyEnabled());
                menu.findItem(R.id.menu_sort).setVisible(true);
                return true;
            }
        });
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
            if (networkStateProvider.isDeviceOnline()) {
                blankFormsListViewModel.syncWithServer().observe(activity, success -> {
                if (success) {
                    Toast.makeText(
                            activity,
                            activity.getString(R.string.form_update_succeeded),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
            } else {
                Toast.makeText(activity, R.string.no_connection, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return false;
        }
    }
}
