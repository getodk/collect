package org.odk.collect.android.formlists.savedformlist

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import org.odk.collect.android.R
import org.odk.collect.android.formlists.sorting.FormListSortingBottomSheetDialog
import org.odk.collect.android.formlists.sorting.FormListSortingOption

class SavedFormListListMenuProvider(private val context: Context, private val viewModel: SavedFormListViewModel) : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.saved_form_list_menu, menu)

        menu.findItem(R.id.menu_filter).apply {
            setOnActionExpandListener(object : OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                    menu.findItem(R.id.menu_sort).isVisible = false
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                    menu.findItem(R.id.menu_sort).isVisible = true
                    return true
                }
            })

            (actionView as SearchView).apply {
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String) = false

                    override fun onQueryTextChange(newText: String): Boolean {
                        viewModel.filterText = newText
                        return false
                    }
                })
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_sort -> {
                FormListSortingBottomSheetDialog(
                    context,
                    SavedFormListViewModel.SortOrder.entries.map { getFormListSortingOption(it) },
                    viewModel.sortOrder.ordinal
                ) {
                    viewModel.sortOrder = SavedFormListViewModel.SortOrder.entries[it]
                }.show()
                true
            }

            else -> false
        }
    }

    private fun getFormListSortingOption(it: SavedFormListViewModel.SortOrder) =
        when (it) {
            SavedFormListViewModel.SortOrder.NAME_ASC -> FormListSortingOption(
                R.drawable.ic_sort_by_alpha,
                org.odk.collect.strings.R.string.sort_by_name_asc
            )

            SavedFormListViewModel.SortOrder.NAME_DESC -> FormListSortingOption(
                R.drawable.ic_sort_by_alpha,
                org.odk.collect.strings.R.string.sort_by_name_desc
            )

            SavedFormListViewModel.SortOrder.DATE_DESC -> FormListSortingOption(
                R.drawable.ic_access_time,
                org.odk.collect.strings.R.string.sort_by_date_desc
            )

            SavedFormListViewModel.SortOrder.DATE_ASC -> FormListSortingOption(
                R.drawable.ic_access_time,
                org.odk.collect.strings.R.string.sort_by_date_asc
            )
        }
}
