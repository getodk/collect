package org.odk.collect.android.draw

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rarepebble.colorpicker.ColorPickerView
import org.odk.collect.androidshared.R

class PenColorPickerDialog : DialogFragment() {

    val model by activityViewModels<PenColorPickerViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val picker = ColorPickerView(requireContext()).apply {
            color = model.penColor.value
            showAlpha(false)
            showHex(false)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(picker)
            .setTitle(R.string.project_color)
            .setPositiveButton(R.string.ok) { _, _ -> model.setPenColor(picker.color) }
            .create()
    }
}
