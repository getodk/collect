package org.odk.collect.android.formentry.repeats

import android.content.Context
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.strings.R

object AddRepeatDialog {

    @JvmStatic
    fun show(context: Context, groupLabel: String?, listener: Listener) {
        val alertDialog = MaterialAlertDialogBuilder(context).create()

        val repeatListener =
            DialogInterface.OnClickListener { _: DialogInterface?, i: Int ->
                when (i) {
                    DialogInterface.BUTTON_POSITIVE -> listener.onAddRepeatClicked()
                    DialogInterface.BUTTON_NEGATIVE -> listener.onCancelClicked()
                }
            }

        val dialogMessage = if (groupLabel.isNullOrBlank()) {
            context.getString(R.string.add_another_question)
        } else {
            context.getString(R.string.add_repeat_question, groupLabel)
        }

        alertDialog.setTitle(dialogMessage)

        alertDialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            context.getString(R.string.add_repeat),
            repeatListener
        )
        alertDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            context.getString(R.string.cancel),
            repeatListener
        )

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    interface Listener {
        fun onAddRepeatClicked()

        fun onCancelClicked()
    }
}
