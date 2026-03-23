package org.odk.collect.android.formentry

import android.content.Context
import android.view.LayoutInflater
import androidx.core.widget.NestedScrollView
import org.odk.collect.android.R
import org.odk.collect.android.databinding.FormEntryEndBinding
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent

class FormEndView(
    context: Context,
    formTitle: String,
    isFormEditableAfterFinalization: Boolean,
    formEndViewModel: FormEndViewModel,
    private val listener: Listener
) : SwipeHandler.View(context) {

    private val binding = FormEntryEndBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.composeView.setContextThemedContent { 
            FormEnd(
                formTitle = formTitle,
                isEditableAfterFinalization = isFormEditableAfterFinalization,
                shouldBeSentAutomatically = formEndViewModel.shouldFormBeSentAutomatically(),
                saveAsDraftEnabled = formEndViewModel.isSaveDraftEnabled(),
                finalizeEnabled = formEndViewModel.isFinalizeEnabled(),
                onSave = { listener.onSaveClicked(it) }
            )
        }
    }

    override fun shouldSuppressFlingGesture() = false

    override fun verticalScrollView(): NestedScrollView? {
        return findViewById(R.id.scroll_view)
    }

    interface Listener {
        fun onSaveClicked(markAsFinalized: Boolean)
    }
}
