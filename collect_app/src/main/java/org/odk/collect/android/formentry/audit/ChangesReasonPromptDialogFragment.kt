package org.odk.collect.android.formentry.audit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.R
import org.odk.collect.android.formentry.saving.FormSaveViewModel
import org.odk.collect.androidshared.ui.EdgeToEdge.applyBottomInsets
import org.odk.collect.material.MaterialFullScreenDialogFragment

class ChangesReasonPromptDialogFragment(
    private val viewModelFactory: ViewModelProvider.Factory
) : MaterialFullScreenDialogFragment() {

    private lateinit var viewModel: FormSaveViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[FormSaveViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.changes_reason_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.applyBottomInsets()

        getToolbar().apply {
            title = viewModel.formName
            setNavigationIcon(org.odk.collect.icons.R.drawable.ic_close)
            setNavigationContentDescription(org.odk.collect.strings.R.string.close)
            inflateMenu(R.menu.changes_reason_dialog)
            setOnMenuItemClickListener {
                viewModel.resumeSave()
                true
            }
        }

        view.findViewById<EditText>(R.id.reason).apply {
            setText(viewModel.reason)
            doAfterTextChanged { viewModel.reason = it.toString() }
            requestFocus()
        }
    }

    override fun getToolbar(): Toolbar {
        return requireView().findViewById(org.odk.collect.androidshared.R.id.toolbar)
    }

    override fun onCloseClicked() {
        dismiss()
    }

    override fun shouldShowSoftKeyboard(): Boolean {
        return true
    }
}
