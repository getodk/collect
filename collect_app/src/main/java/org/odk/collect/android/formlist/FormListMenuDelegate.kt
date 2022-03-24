package org.odk.collect.android.formlist

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.ComponentActivity
import org.odk.collect.android.R
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.android.utilities.MenuDelegate

class FormListMenuDelegate(
    private val activity: ComponentActivity,
    private val viewModel: FormListViewModel,
    private val networkStateProvider: NetworkStateProvider
) : MenuDelegate {
    private var outOfSync = false
    private var syncing = false

    override fun onCreateOptionsMenu(menuInflater: MenuInflater, menu: Menu) {
        menu.findItem(R.id.menu_filter)
            .setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                    menu.findItem(R.id.menu_refresh).isVisible = false
                    menu.findItem(R.id.menu_sort).isVisible = false
                    return true
                }

                override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                    menu.findItem(R.id.menu_refresh).isVisible = viewModel.isMatchExactlyEnabled()
                    menu.findItem(R.id.menu_sort).isVisible = true
                    return true
                }
            })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val refreshItem = menu.findItem(R.id.menu_refresh)
        refreshItem.isVisible = viewModel.isMatchExactlyEnabled()
        refreshItem.isEnabled = !syncing
        if (outOfSync) {
            refreshItem.setIcon(R.drawable.ic_baseline_refresh_error_24)
        } else {
            refreshItem.setIcon(R.drawable.ic_baseline_refresh_24)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.menu_refresh) {
            if (networkStateProvider.isDeviceOnline) {
                viewModel.syncWithServer().observe(activity) { success: Boolean ->
                    if (success) {
                        Toast.makeText(
                            activity,
                            activity.getString(R.string.form_update_succeeded),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    activity,
                    R.string.no_connection,
                    Toast.LENGTH_SHORT
                ).show()
            }
            true
        } else {
            false
        }
    }

    init {
        viewModel.isSyncing().observe(activity) { syncing: Boolean ->
            this.syncing = syncing
            activity.invalidateOptionsMenu()
        }

        viewModel.isOutOfSync().observe(activity) { outOfSync: Boolean ->
            this.outOfSync = outOfSync
            activity.invalidateOptionsMenu()
        }
    }
}
