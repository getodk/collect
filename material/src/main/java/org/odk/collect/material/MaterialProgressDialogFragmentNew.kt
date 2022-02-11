package org.odk.collect.material

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing

/**
 * Provides a reusable progress dialog implemented with [MaterialAlertDialogBuilder]. Progress
 * dialogs don't appear in the Material guidelines/specs due to the design language's instistance
 * that progress shouldn't block the user - this is pretty unrealistic for the app in it's current
 * state so having a reliable "Material" version of the Android progress dialog is useful.
 */
open class MaterialProgressDialogFragmentNew private constructor(
    private val title: String?,
    private val message: String?,
    private val cancelable: Boolean,
    private val positiveButtonTitle: String?,
    private val positiveButtonListener: DialogInterface.OnClickListener?,
    private val negativeButtonTitle: String?,
    private val negativeButtonListener: DialogInterface.OnClickListener?
) : DialogFragment() {

    private lateinit var dialogView: View

    data class Builder(
        var title: String? = null,
        var message: String? = null,
        var cancelable: Boolean = true,
        var positiveButtonTitle: String? = null,
        var positiveButtonListener: DialogInterface.OnClickListener? = null,
        var negativeButtonTitle: String? = null,
        var negativeButtonListener: DialogInterface.OnClickListener? = null
    ) {
        fun title(title: String) = apply { this.title = title }
        fun message(message: String) = apply { this.message = message }
        fun cancelable(cancelable: Boolean) = apply { this.cancelable = cancelable }
        fun positiveButton(positiveButtonTitle: String, positiveButtonListener: DialogInterface.OnClickListener) = apply {
            this.positiveButtonTitle = positiveButtonTitle
            this.positiveButtonListener = positiveButtonListener
        }
        fun negativeButton(negativeButtonTitle: String, negativeButtonListener: DialogInterface.OnClickListener) = apply {
            this.negativeButtonTitle = negativeButtonTitle
            this.negativeButtonListener = negativeButtonListener
        }

        fun show(fragmentManager: FragmentManager) {
            val dialog = MaterialProgressDialogFragmentNew(
                title,
                message,
                cancelable,
                positiveButtonTitle,
                positiveButtonListener,
                negativeButtonTitle,
                negativeButtonListener
            )
            showIfNotShowing(
                dialog,
                MaterialProgressDialogFragmentNew::class.java,
                fragmentManager
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        dialogView = requireActivity().layoutInflater.inflate(R.layout.progress_dialog, null, false).apply {
            (findViewById<View>(R.id.message) as TextView).text = message
        }
        isCancelable = cancelable

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(dialogView)
            .create()

        if (positiveButtonTitle != null) {
            dialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                positiveButtonTitle,
                positiveButtonListener
            )
        }

        if (negativeButtonTitle != null) {
            dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                negativeButtonTitle,
                negativeButtonListener
            )
        }

        return dialog
    }

    fun updateMessage(message: String) {
        (dialogView.findViewById<View>(R.id.message) as TextView).text = message
    }
}
