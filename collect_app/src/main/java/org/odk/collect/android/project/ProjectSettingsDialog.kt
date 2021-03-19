package org.odk.collect.android.project

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import org.odk.collect.android.R

class ProjectSettingsDialog : DialogFragment() {
    interface ProjectSettingsDialogListener {
        fun openGeneralSettings()

        fun openAdminSettings()
    }

    private lateinit var listener: ProjectSettingsDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ProjectSettingsDialogListener) {
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.project_settings_dialog_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.close_icon).setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.general_settings_button).setOnClickListener {
            listener.openGeneralSettings()
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.admin_settings_button).setOnClickListener {
            listener.openAdminSettings()
            dismiss()
        }
    }

    companion object {
        const val TAG = "ProjectSettingsDialog"
    }
}
