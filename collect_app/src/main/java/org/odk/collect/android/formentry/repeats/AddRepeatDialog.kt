package org.odk.collect.android.formentry.repeats

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickSafeMaterialButton
import org.odk.collect.strings.R.string

object AddRepeatDialog {

    @JvmStatic
    fun show(context: Context, groupLabel: String?, listener: Listener) {
        val view = LayoutInflater.from(context).inflate(R.layout.add_repeat_dialog_layout, null)

        view.findViewById<MaterialTextView>(R.id.message).text = if (groupLabel.isNullOrBlank()) {
            context.getString(string.add_another_question)
        } else {
            context.getString(string.add_repeat_question, groupLabel)
        }

        val alertDialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(false)
            .create()

        view.findViewById<MultiClickSafeMaterialButton>(R.id.add_button).setOnClickListener {
            listener.onAddRepeatClicked()
            alertDialog.dismiss()
        }

        view.findViewById<MultiClickSafeMaterialButton>(R.id.do_not_add_button).setOnClickListener {
            listener.onCancelClicked()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    interface Listener {
        fun onAddRepeatClicked()

        fun onCancelClicked()
    }
}
