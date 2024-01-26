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
                    emptyList(),
                    0
                ) {}.show()
                true
            }

            else -> false
        }
    }
}
