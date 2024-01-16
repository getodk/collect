package org.odk.collect.draw

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rarepebble.colorpicker.ColorPickerView

internal class PenColorPickerDialog(private val model: PenColorPickerViewModel) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val picker = ColorPickerView(requireContext()).apply {
            color = model.penColor.value
            showAlpha(false)
            showHex(false)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(picker)
            .setTitle(org.odk.collect.strings.R.string.project_color)
            .setPositiveButton(org.odk.collect.strings.R.string.ok) { _, _ -> model.setPenColor(picker.color) }
            .create()
    }
}
