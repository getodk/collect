package org.odk.collect.android.formentry

import android.app.Activity
import android.content.DialogInterface
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.collect.ImmutableList
import org.odk.collect.android.R
import org.odk.collect.android.adapters.IconMenuListAdapter
import org.odk.collect.android.adapters.model.IconMenuItem
import org.odk.collect.android.formentry.saving.FormSaveViewModel
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys

object QuitFormDialog {

    @JvmStatic
    fun show(
        activity: Activity,
        formSaveViewModel: FormSaveViewModel,
        formEntryViewModel: FormEntryViewModel,
        settingsProvider: SettingsProvider,
        currentProjectProvider: CurrentProjectProvider,
        onSaveChangesClicked: Runnable?
    ): AlertDialog {
        return create(
            activity,
            formSaveViewModel,
            formEntryViewModel,
            settingsProvider,
            currentProjectProvider,
            onSaveChangesClicked
        ).also {
            it.show()
        }
    }

    private fun create(
        activity: Activity,
        formSaveViewModel: FormSaveViewModel,
        formEntryViewModel: FormEntryViewModel,
        settingsProvider: SettingsProvider,
        currentProjectProvider: CurrentProjectProvider,
        onSaveChangesClicked: Runnable?
    ): AlertDialog {
        val items: List<IconMenuItem> = if (
            settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_SAVE_MID)
        ) {
            ImmutableList.of(
                IconMenuItem(R.drawable.ic_save, R.string.save_as_draft),
                getDiscardItem(formSaveViewModel)
            )
        } else {
            ImmutableList.of(getDiscardItem(formSaveViewModel))
        }

        val listView = DialogUtils.createActionListView(activity)

        val adapter = IconMenuListAdapter(activity, items)
        listView.adapter = adapter

        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(activity.resources.getString(R.string.quit_form_title))
            .setNegativeButton(activity.resources.getString(R.string.do_not_exit)) { dialog: DialogInterface, id: Int ->
                dialog.dismiss()
            }
            .setView(listView)
            .create()

        listView.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val item = adapter.getItem(position) as IconMenuItem
                if (item.textResId == R.string.save_as_draft) {
                    onSaveChangesClicked?.run()
                } else {
                    formSaveViewModel.ignoreChanges()
                    formEntryViewModel.exit()
                    activity.finish()
                }

                dialog.dismiss()
            }

        return dialog
    }

    private fun getDiscardItem(formSaveViewModel: FormSaveViewModel): IconMenuItem {
        return if (formSaveViewModel.hasSaved()) {
            IconMenuItem(R.drawable.ic_delete, R.string.discard_changes)
        } else {
            IconMenuItem(R.drawable.ic_delete, R.string.do_not_save)
        }
    }
}
