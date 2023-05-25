package org.odk.collect.android.formentry

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.databinding.QuitFormDialogLayoutBinding
import org.odk.collect.android.formentry.saving.FormSaveViewModel

object QuitFormDialog {

    @JvmStatic
    fun show(
        activity: Activity,
        formSaveViewModel: FormSaveViewModel,
        formEntryViewModel: FormEntryViewModel,
        onSaveChangesClicked: Runnable?
    ): AlertDialog {
        return create(
            activity,
            formSaveViewModel,
            formEntryViewModel,
            onSaveChangesClicked
        ).also {
            it.show()
        }
    }

    private fun create(
        activity: Activity,
        formSaveViewModel: FormSaveViewModel,
        formEntryViewModel: FormEntryViewModel,
        onSaveChangesClicked: Runnable?
    ): AlertDialog {
        val binding = QuitFormDialogLayoutBinding.inflate(activity.layoutInflater)

        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(activity.resources.getString(R.string.quit_form_title))
            .setView(binding.root)
            .create()

        binding.discardChanges.setText(
            if (formSaveViewModel.hasSaved()) {
                R.string.discard_changes
            } else {
                R.string.do_not_save
            }
        )

        binding.discardChanges.setOnClickListener {
            formSaveViewModel.ignoreChanges()
            formEntryViewModel.exit()
            activity.finish()
            dialog.dismiss()
        }

        binding.saveChanges.setOnClickListener {
            onSaveChangesClicked?.run()
        }

        return dialog
    }
}
