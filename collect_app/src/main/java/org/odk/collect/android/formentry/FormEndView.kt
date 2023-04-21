package org.odk.collect.android.formentry

import android.content.Context
import android.view.LayoutInflater
import androidx.core.widget.NestedScrollView
import org.odk.collect.android.R
import org.odk.collect.android.databinding.FormEntryEndBinding
import org.odk.collect.android.listeners.SwipeHandler

class FormEndView(
    context: Context,
    formTitle: String,
    isSaveAsDraftEnabled: Boolean,
    isFinalizeEnabled: Boolean,
    private val listener: Listener
) : SwipeHandler.View(context) {

    private val binding = FormEntryEndBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.description.text = context.getString(R.string.save_enter_data_description, formTitle)

        binding.saveAsDraft.isEnabled = isSaveAsDraftEnabled
        binding.saveAsDraft.setOnClickListener {
            listener.onSaveClicked(false)
        }

        binding.finalize.isEnabled = isFinalizeEnabled
        binding.finalize.setOnClickListener {
            listener.onSaveClicked(true)
        }
    }

    override fun shouldSuppressFlingGesture() = false

    override fun getVerticalScrollView(): NestedScrollView? {
        return findViewById(R.id.scroll_view)
    }

    interface Listener {
        fun onSaveClicked(markAsFinalized: Boolean)
    }
}
