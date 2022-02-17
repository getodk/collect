package org.odk.collect.android.draw

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rarepebble.colorpicker.ColorPickerView
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.androidshared.R
import javax.inject.Inject

class PenColorPickerDialog : DialogFragment() {
    @Inject
    lateinit var factory: PenColorPickerViewModel.Factory

    lateinit var model: PenColorPickerViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
        model = ViewModelProvider(requireActivity(), factory)[PenColorPickerViewModel::class.java]
    }

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
