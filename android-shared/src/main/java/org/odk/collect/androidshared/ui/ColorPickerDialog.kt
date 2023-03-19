package org.odk.collect.androidshared

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.androidshared.databinding.ColorPickerDialogLayoutBinding
import java.lang.Exception

class ColorPickerDialog : DialogFragment() {
    lateinit var binding: ColorPickerDialogLayoutBinding

    val model: ColorPickerViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = ColorPickerDialogLayoutBinding.inflate(LayoutInflater.from(context))
        binding.hexColor.doOnTextChanged { color, _, _, _ ->
            updateCurrentColorCircle("#$color")
        }
        binding.currentColor.text = requireArguments().getString(CURRENT_ICON)!!

        fixHexColorPrefix()
        setListeners()
        setCurrentColor(requireArguments().getString(CURRENT_COLOR)!!)

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.project_color)
            .setNegativeButton(R.string.cancel) { _, _ -> dismiss() }
            .setPositiveButton(R.string.ok) { _, _ -> model.pickColor("#${binding.hexColor.text}") }
            .create()
    }

    private fun setListeners() {
        binding.color1.setOnClickListener { setCurrentColor(R.color.color1) }
        binding.color2.setOnClickListener { setCurrentColor(R.color.color2) }
        binding.color3.setOnClickListener { setCurrentColor(R.color.color3) }
        binding.color4.setOnClickListener { setCurrentColor(R.color.color4) }
        binding.color5.setOnClickListener { setCurrentColor(R.color.color5) }
        binding.color6.setOnClickListener { setCurrentColor(R.color.color6) }
        binding.color7.setOnClickListener { setCurrentColor(R.color.color7) }
        binding.color8.setOnClickListener { setCurrentColor(R.color.color8) }
        binding.color9.setOnClickListener { setCurrentColor(R.color.color9) }
        binding.color10.setOnClickListener { setCurrentColor(R.color.color10) }
        binding.color11.setOnClickListener { setCurrentColor(R.color.color11) }
        binding.color12.setOnClickListener { setCurrentColor(R.color.color12) }
        binding.color13.setOnClickListener { setCurrentColor(R.color.color13) }
        binding.color14.setOnClickListener { setCurrentColor(R.color.color14) }
        binding.color15.setOnClickListener { setCurrentColor(R.color.color15) }
    }

    private fun setCurrentColor(color: Int) {
        setCurrentColor("#${Integer.toHexString(ContextCompat.getColor(requireContext(), color)).substring(2)}")
    }

    private fun setCurrentColor(color: String) {
        binding.hexColor.setText(color.substring(1))
    }

    private fun updateCurrentColorCircle(color: String) {
        try {
            (binding.currentColor.background as GradientDrawable).setColor(Color.parseColor(color))
            binding.hexColor.error = null
            (dialog as? AlertDialog)?.also {
                it.getButton(AlertDialog.BUTTON_POSITIVE).alpha = 1f
                it.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
            }
        } catch (e: Exception) {
            binding.hexColor.error = getString(R.string.invalid_hex_code)
            (dialog as? AlertDialog)?.also {
                it.getButton(AlertDialog.BUTTON_POSITIVE).alpha = 0.3f
                it.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            }
        }
    }

    // https://github.com/material-components/material-components-android/issues/773#issuecomment-603759649
    private fun fixHexColorPrefix() {
        val prefixView = binding.hexColorLayout.findViewById<AppCompatTextView>(com.google.android.material.R.id.textinput_prefix_text)
        prefixView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        prefixView.gravity = Gravity.CENTER
    }

    companion object {
        const val CURRENT_COLOR = "CURRENT_COLOR"
        const val CURRENT_ICON = "CURRENT_ICON"
    }
}

class ColorPickerViewModel : ViewModel() {
    private val _pickedColor = MutableLiveData<String>()
    val pickedColor: LiveData<String> = _pickedColor

    fun pickColor(color: String) {
        _pickedColor.value = color
    }
}
