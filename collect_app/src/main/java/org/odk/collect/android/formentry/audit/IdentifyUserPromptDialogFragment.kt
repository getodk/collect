package org.odk.collect.android.formentry.audit

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.R
import org.odk.collect.material.MaterialFullScreenDialogFragment

class IdentifyUserPromptDialogFragment : MaterialFullScreenDialogFragment() {
    private lateinit var viewModel: IdentityPromptViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.identify_user_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar?.apply {
            setTitle(viewModel.formTitle)
            setNavigationIcon(org.odk.collect.icons.R.drawable.ic_close)
            setNavigationContentDescription(org.odk.collect.strings.R.string.close)
        }

        view.findViewById<EditText>(R.id.identity).apply {
            setText(viewModel.user)
            doAfterTextChanged { text ->
                viewModel.setIdentity(text.toString())
            }
            setOnEditorActionListener { textView: TextView?, i: Int, keyEvent: KeyEvent? ->
                viewModel.done()
                true
            }
            requestFocus()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = ViewModelProvider(requireActivity())[IdentityPromptViewModel::class.java]
        viewModel.requiresIdentity.observe(this, Observer { requiresIdentity: Boolean? ->
            if (!requiresIdentity!!) {
                dismiss()
            }
        })
    }

    override fun onCloseClicked() {
        dismiss()
        viewModel.promptDismissed()
    }

    override fun onBackPressed() {
        dismiss()
        viewModel.promptDismissed()
    }

    override fun getToolbar(): Toolbar? {
        return view?.findViewById(org.odk.collect.androidshared.R.id.toolbar)
    }

    override fun shouldShowSoftKeyboard(): Boolean {
        return true
    }
}
