package org.odk.collect.android.formentry

import android.content.Context
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import org.odk.collect.android.R
import org.odk.collect.android.databinding.FormEntryEndBinding

class FormEndView(
    context: Context,
    formTitle: String,
    formEndViewModel: FormEndViewModel,
    private val listener: Listener
) : SwipeHandler.View(context) {

    private val binding = FormEntryEndBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.description.text = context.getString(R.string.save_enter_data_description, formTitle)

        binding.saveAsDraft.isVisible = formEndViewModel.isSaveDraftEnabled()
        binding.saveAsDraft.setOnClickListener {
            listener.onSaveClicked(false)
        }

        binding.finalize.isVisible = formEndViewModel.isFinalizeEnabled()
        binding.finalize.setOnClickListener {
            listener.onSaveClicked(true)
        }
        if (formEndViewModel.shouldFormBeSentAutomatically()) {
            binding.finalize.text = context.getString(R.string.send)
        }

        binding.spaceBox.isVisible = binding.saveAsDraft.isVisible && binding.finalize.isVisible
    }

    override fun shouldSuppressFlingGesture() = false

    override fun verticalScrollView(): NestedScrollView? {
        return findViewById(R.id.scroll_view)
    }

    interface Listener {
        fun onSaveClicked(markAsFinalized: Boolean)
    }
}
