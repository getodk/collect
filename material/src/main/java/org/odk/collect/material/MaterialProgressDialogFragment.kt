package org.odk.collect.material

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.Serializable

/**
 * Provides a reusable progress dialog implemented with [MaterialAlertDialogBuilder]. Progress
 * dialogs don't appear in the Material guidelines/specs due to the design language's instistance
 * that progress shouldn't block the user - this is pretty unrealistic for the app in it's current
 * state so having a reliable "Material" version of the Android progress dialog is useful.
 */
open class MaterialProgressDialogFragment : DialogFragment() {

    var dialogView: View? = null
        private set
    private var onPositiveButtonClickListener: DialogInterface.OnClickListener? = null
    private var onNegativeButtonClickListener: DialogInterface.OnClickListener? = null

    /**
     * Override to have something cancelled when the ProgressDialog's cancel button is pressed
     */
    protected open val onCancelCallback: OnCancelCallback?
        get() = null

    /**
     * Override to show cancel button with returned text
     */
    protected open val cancelButtonText: String?
        get() = null

    var title: String?
        get() = requireArguments().getString(TITLE)
        set(title) {
            setArgument(TITLE, title)
            setupView()
        }

    var message: String?
        get() = requireArguments().getString(MESSAGE)
        set(message) {
            setArgument(MESSAGE, message)
            setupView()
        }

    @get:DrawableRes
    var icon: Int
        get() = requireArguments().getInt(ICON)
        set(iconId) {
            setArgument(ICON, iconId)
            setupView()
        }

    fun setPositiveButton(text: String, listener: DialogInterface.OnClickListener?) {
        setArgument(POSITIVE_BUTTON_TEXT, text)
        onPositiveButtonClickListener = listener
        setupView()
    }

    fun setNegativeButton(text: String, listener: DialogInterface.OnClickListener?) {
        setArgument(NEGATIVE_BUTTON_TEXT, text)
        onNegativeButtonClickListener = listener
        setupView()
    }

    override fun setCancelable(cancelable: Boolean) {
        setArgument(CANCELABLE, cancelable)
        super.setCancelable(cancelable)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        dialogView = requireActivity().layoutInflater.inflate(R.layout.progress_dialog, null, false)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()
        setupView(dialog)
        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        val onCancelCallback = onCancelCallback
        onCancelCallback?.cancel()
    }

    private fun setupView() {
        val dialog = dialog as AlertDialog?
        dialog?.let { setupView(it) }
    }

    private fun setupView(dialog: AlertDialog) {
        if (arguments != null && requireArguments().getString(TITLE) != null) {
            dialog.setTitle(requireArguments().getString(TITLE))
        }
        if (arguments != null && requireArguments().getString(MESSAGE) != null) {
            (dialogView!!.findViewById<View>(R.id.message) as TextView).text =
                requireArguments().getString(
                    MESSAGE
                )
        }
        if (arguments != null && requireArguments().getInt(ICON, -1) != -1) {
            dialog.setIcon(requireArguments().getInt(ICON))
        }
        if (arguments != null) {
            isCancelable = requireArguments().getBoolean(CANCELABLE)
        }
        if (cancelButtonText != null) {
            dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                cancelButtonText
            ) { _: DialogInterface?, _: Int ->
                dismiss()
                onCancelCallback!!.cancel()
            }
        } else if (arguments != null && requireArguments().getString(NEGATIVE_BUTTON_TEXT) != null) {
            dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                requireArguments().getString(NEGATIVE_BUTTON_TEXT),
                onNegativeButtonClickListener
            )
        }
        if (arguments != null && requireArguments().getString(POSITIVE_BUTTON_TEXT) != null) {
            dialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                requireArguments().getString(POSITIVE_BUTTON_TEXT),
                onPositiveButtonClickListener
            )
        }
    }

    private fun setArgument(key: String, value: Serializable?) {
        if (arguments == null) {
            arguments = Bundle()
        }
        requireArguments().putSerializable(key, value)
    }

    interface OnCancelCallback {
        fun cancel(): Boolean
    }

    companion object {
        private const val TITLE = "title"
        private const val MESSAGE = "message"
        private const val CANCELABLE = "true"
        private const val ICON = "icon"
        private const val POSITIVE_BUTTON_TEXT = "positive_button_text"
        private const val NEGATIVE_BUTTON_TEXT = "negative_button_text"
    }
}
