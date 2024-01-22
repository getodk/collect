package org.odk.collect.android.formlists.savedformlist

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import org.odk.collect.android.R
import org.odk.collect.android.formlists.sorting.FormListSortingBottomSheetDialog

class SavedFormListListMenuProvider(private val context: Context) : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.form_list_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        FormListSortingBottomSheetDialog(
            context,
            emptyList(),
            0
        ) {}.show()
        return true
    }
}
