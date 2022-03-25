package org.odk.collect.android.formlist

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.odk.collect.android.R
import org.odk.collect.android.adapters.SortDialogAdapter
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
        menu.findItem(R.id.menu_filter).apply {
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
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

            (actionView as SearchView).apply {
                queryHint = activity.resources.getString(R.string.search)
                maxWidth = Int.MAX_VALUE
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String) = false

                    override fun onQueryTextChange(newText: String): Boolean {
                        viewModel.filterText.value = newText
                        return false
                    }
                })
            }
        }
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
        return when (item.itemId) {
            R.id.menu_refresh -> {
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
            }
            R.id.menu_sort -> {
                showBottomSheetDialog()
                true
            }
            else -> false
        }
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(activity)
        val sheetView: View = activity.layoutInflater.inflate(R.layout.bottom_sheet, null)
        val recyclerView: RecyclerView = sheetView.findViewById(R.id.recyclerView)

        val adapter =
            SortDialogAdapter(
                activity, recyclerView, viewModel.sortingOptions, viewModel.sortingOrder.value
            ) { holder, position ->
                holder.updateItemColor(position)
                viewModel.sortingOrder.value = position
                bottomSheetDialog.dismiss()
            }
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
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
