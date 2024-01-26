package org.odk.collect.android.formlists.blankformlist

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import org.odk.collect.android.R
import org.odk.collect.android.formlists.sorting.FormListSortingBottomSheetDialog
import org.odk.collect.android.formlists.sorting.FormListSortingOption
import org.odk.collect.androidshared.network.NetworkStateProvider
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard

class BlankFormListMenuProvider(
    private val activity: ComponentActivity,
    private val viewModel: BlankFormListViewModel,
    private val networkStateProvider: NetworkStateProvider? = null
) : MenuProvider {

    private var outOfSync = false
    private var syncing = false

    init {
        viewModel.isLoading.observe(activity) { isLoading: Boolean ->
            this.syncing = isLoading
            activity.invalidateOptionsMenu()
        }

        viewModel.isOutOfSyncWithServer().observe(activity) { outOfSync: Boolean ->
            this.outOfSync = outOfSync
            activity.invalidateOptionsMenu()
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.blank_form_list_menu, menu)

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
                queryHint = activity.resources.getString(org.odk.collect.strings.R.string.search)
                maxWidth = Int.MAX_VALUE
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String) = false

                    override fun onQueryTextChange(newText: String): Boolean {
                        viewModel.filterText = newText
                        return false
                    }
                })
            }
            viewModel.filterText = ""
        }
    }

    override fun onPrepareMenu(menu: Menu) {
        val refreshItem = menu.findItem(R.id.menu_refresh)
        refreshItem.isVisible = viewModel.isMatchExactlyEnabled()
        refreshItem.isEnabled = !syncing
        if (outOfSync) {
            refreshItem.setIcon(R.drawable.ic_baseline_refresh_error_24)
        } else {
            refreshItem.setIcon(R.drawable.ic_baseline_refresh_24)
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if (!MultiClickGuard.allowClick(javaClass.name)) {
            return true
        }

        return when (item.itemId) {
            R.id.menu_refresh -> {
                if (networkStateProvider?.isDeviceOnline == true) {
                    viewModel.syncWithServer().observe(activity) { success: Boolean ->
                        if (success) {
                            ToastUtils.showShortToast(activity, org.odk.collect.strings.R.string.form_update_succeeded)
                        }
                    }
                } else {
                    ToastUtils.showShortToast(activity, org.odk.collect.strings.R.string.no_connection)
                }
                true
            }
            R.id.menu_sort -> {
                FormListSortingBottomSheetDialog(
                    activity,
                    listOf(
                        FormListSortingOption(
                            R.drawable.ic_sort_by_alpha,
                            org.odk.collect.strings.R.string.sort_by_name_asc
                        ),
                        FormListSortingOption(
                            R.drawable.ic_sort_by_alpha,
                            org.odk.collect.strings.R.string.sort_by_name_desc
                        ),
                        FormListSortingOption(
                            R.drawable.ic_access_time,
                            org.odk.collect.strings.R.string.sort_by_date_desc
                        ),
                        FormListSortingOption(
                            R.drawable.ic_access_time,
                            org.odk.collect.strings.R.string.sort_by_date_asc
                        ),
                        FormListSortingOption(
                            R.drawable.ic_sort_by_last_saved,
                            org.odk.collect.strings.R.string.sort_by_last_saved
                        )
                    ),
                    viewModel.sortingOrder
                ) { newSortingOrder ->
                    viewModel.sortingOrder = newSortingOrder
                }.show()

                true
            }
            else -> false
        }
    }
}
