package org.odk.collect.android.formmanagement.drafts

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.strings.R.plurals
import org.odk.collect.strings.R.string

class DraftsMenuProvider(
    private val context: Context,
    private val bulkFinalizationViewModel: BulkFinalizationViewModel
) : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.drafts, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.bulk_finalize) {
            val draftsCount = bulkFinalizationViewModel.draftsCount!!
            val dialogTitle = context.resources.getQuantityString(
                plurals.bulk_finalize_confirmation,
                draftsCount,
                draftsCount
            )

            MaterialAlertDialogBuilder(context)
                .setTitle(dialogTitle)
                .setMessage(string.bulk_finalize_explanation)
                .setPositiveButton(string.finalize) { _, _ ->
                    bulkFinalizationViewModel.finalizeAllDrafts()
                }
                .setNegativeButton(string.cancel, null)
                .show()

            return true
        }

        return false
    }
}
