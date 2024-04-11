package org.odk.collect.entities

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.entities.databinding.AddEntitiesDialogLayoutBinding
import org.odk.collect.strings.R.string

class AddEntityListDialogFragment(private val entitiesViewModel: EntitiesViewModel) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = AddEntitiesDialogLayoutBinding.inflate(layoutInflater)

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton(string.add) { _, _ ->
                entitiesViewModel.addEntityList(binding.entityListName.text.toString())
            }
            .create()
    }
}
