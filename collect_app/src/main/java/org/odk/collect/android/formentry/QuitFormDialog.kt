package org.odk.collect.android.formentry

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.databinding.QuitFormDialogLayoutBinding
import org.odk.collect.android.formentry.saving.FormSaveViewModel
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys

object QuitFormDialog {

    @JvmStatic
    fun show(
        activity: Activity,
        formSaveViewModel: FormSaveViewModel,
        formEntryViewModel: FormEntryViewModel,
        settingsProvider: SettingsProvider,
        onSaveChangesClicked: Runnable?
    ): AlertDialog {
        return create(
            activity,
            formSaveViewModel,
            formEntryViewModel,
            settingsProvider,
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
        onSaveChangesClicked: Runnable?
    ): AlertDialog {
        val saveAsDraft = settingsProvider.getProtectedSettings()
            .getBoolean(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT)
        val lastSavedTime = formSaveViewModel.lastSavedTime

        val binding = QuitFormDialogLayoutBinding.inflate(activity.layoutInflater)
        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(
                if (saveAsDraft) {
                    R.string.quit_form_title
                } else {
                    R.string.quit_form_continue_title
                }
            )
            .setView(binding.root)
            .create()

        binding.saveExplanation.text = if (lastSavedTime != null) {
            activity.getString(R.string.save_explanation_with_last_saved, lastSavedTime)
        } else if (!saveAsDraft) {
            activity.getString(R.string.discard_form_warning)
        } else {
            activity.getString(R.string.save_explanation)
        }

        binding.discardChanges.setText(
            if (lastSavedTime != null) {
                R.string.discard_changes
            } else {
                R.string.do_not_save
            }
        )

        binding.keepEditing.setOnClickListener {
            dialog.dismiss()
        }

        binding.discardChanges.setOnClickListener {
            formSaveViewModel.ignoreChanges()
            formEntryViewModel.exit()
            activity.finish()
            dialog.dismiss()
        }

        binding.saveChanges.isVisible = saveAsDraft
        binding.saveChanges.setOnClickListener {
            onSaveChangesClicked?.run()
        }

        return dialog
    }
}
