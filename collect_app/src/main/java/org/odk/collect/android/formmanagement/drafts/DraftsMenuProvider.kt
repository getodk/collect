package org.odk.collect.android.formmanagement.drafts

import android.annotation.SuppressLint
import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.strings.R.plurals
import org.odk.collect.strings.R.string

class DraftsMenuProvider(
    private val context: Context,
    private val onFinalizeAll: Runnable
) : MenuProvider {

    var draftsCount: Int? = null

    @SuppressLint("RestrictedApi")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.drafts, menu)

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
    }

    override fun onPrepareMenu(menu: Menu) {
        if (draftsCount == null || draftsCount == 0) {
            menu.findItem(R.id.bulk_finalize).isVisible = false
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.bulk_finalize) {
            draftsCount?.also {
                val dialogTitle = context.resources.getQuantityString(
                    plurals.bulk_finalize_confirmation,
                    it,
                    it
                )

                MaterialAlertDialogBuilder(context)
                    .setTitle(dialogTitle)
                    .setMessage(string.bulk_finalize_explanation)
                    .setPositiveButton(string.finalize) { _, _ ->
                        onFinalizeAll.run()
                    }
                    .setNegativeButton(string.cancel, null)
                    .show()
            }

            return true
        }

        return false
    }
}
