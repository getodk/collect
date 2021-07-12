package org.odk.collect.android.preferences.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.odk.collect.android.R
import org.odk.collect.android.databinding.AdminPasswordDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.android.utilities.SoftKeyboardController
import javax.inject.Inject

class AdminPasswordDialogFragment : DialogFragment() {
    private var callback: AdminPasswordDialogCallback? = null

    @Inject
    lateinit var adminPasswordProvider: AdminPasswordProvider

    @Inject
    lateinit var softKeyboardController: SoftKeyboardController

    lateinit var binding: AdminPasswordDialogLayoutBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
        if (context is AdminPasswordDialogCallback) {
            callback = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AdminPasswordDialogLayoutBinding.inflate(LayoutInflater.from(context))

        binding.editText.post {
            softKeyboardController.showSoftKeyboard(binding.editText)
        }

        binding.checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                binding.editText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                binding.editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle(getString(R.string.enter_admin_password))
            .setPositiveButton(getString(R.string.ok)) { _: DialogInterface?, _: Int ->
                if (adminPasswordProvider.adminPassword == binding.editText.text.toString()) {
                    callback?.onCorrectAdminPassword()
                } else {
                    callback?.onIncorrectAdminPassword()
                }
                dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { _: DialogInterface?, _: Int -> dismiss() }
            .create()
    }

    interface AdminPasswordDialogCallback {
        fun onCorrectAdminPassword()
        fun onIncorrectAdminPassword()
    }
}
