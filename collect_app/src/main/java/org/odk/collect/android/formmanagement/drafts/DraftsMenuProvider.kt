package org.odk.collect.android.formmanagement.drafts

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import org.odk.collect.android.R

class DraftsMenuProvider(private val bulkFinalizationViewModel: BulkFinalizationViewModel) : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.drafts, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.bulk_finalize) {
            bulkFinalizationViewModel.finalizeAllDrafts()
            return true
        }

        return false
    }
}
