package org.odk.collect.android.formmanagement.drafts

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.core.view.MenuProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.strings.R.plurals
import org.odk.collect.strings.R.string

class DraftsMenuProvider(
    private val activity: ComponentActivity,
    private val bulkFinalizationViewModel: BulkFinalizationViewModel
) : MenuProvider {

    private var draftsCount: Int? = null

    init {
        bulkFinalizationViewModel.draftsCount.observe(activity) {
            draftsCount = it
            activity.invalidateOptionsMenu()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.drafts, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        if (draftsCount == null || draftsCount == 0) {
            menu.findItem(R.id.bulk_finalize).isVisible = false
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.bulk_finalize) {
            val draftsCount = bulkFinalizationViewModel.draftsCount.value!!
            val dialogTitle = activity.resources.getQuantityString(
                plurals.bulk_finalize_confirmation,
                draftsCount,
                draftsCount
            )

            MaterialAlertDialogBuilder(activity)
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
