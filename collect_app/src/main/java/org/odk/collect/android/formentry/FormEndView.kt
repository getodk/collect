package org.odk.collect.android.formentry

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import org.odk.collect.android.R
import org.odk.collect.android.databinding.FormEntryEndBinding

class FormEndView(
    context: Context,
    formTitle: String,
    isFormEditableAfterFinalization: Boolean,
    formEndViewModel: FormEndViewModel,
    private val listener: Listener
) : SwipeHandler.View(context) {

    private val binding = FormEntryEndBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.description.text = context.getString(org.odk.collect.strings.R.string.save_enter_data_description, formTitle)

        binding.saveAsDraft.isVisible = formEndViewModel.isSaveDraftEnabled()
        binding.saveAsDraft.setOnClickListener {
            listener.onSaveClicked(false)
        }

        binding.finalize.isVisible = formEndViewModel.isFinalizeEnabled()
        binding.finalize.setOnClickListener {
            listener.onSaveClicked(true)
        }

        binding.divider.isVisible = binding.saveAsDraft.isVisible && binding.finalize.isVisible

        val shouldFormBeSentAutomatically = formEndViewModel.shouldFormBeSentAutomatically()
        if (shouldFormBeSentAutomatically) {
            binding.finalize.text = context.getString(org.odk.collect.strings.R.string.send)
        }

        if (binding.saveAsDraft.isVisible && binding.finalize.isVisible) {
            if (shouldFormBeSentAutomatically) {
                if (isFormEditableAfterFinalization) {
                    setWarning(
                        icon = R.drawable.ic_edit_24,
                        title = org.odk.collect.strings.R.string.form_editing_enabled_after_sending,
                        hint = org.odk.collect.strings.R.string.form_editing_enabled_after_sending_hint
                    )
                } else {
                    setWarning(
                        icon = R.drawable.ic_edit_off_24,
                        title = org.odk.collect.strings.R.string.form_editing_disabled_after_sending,
                        hint = org.odk.collect.strings.R.string.form_editing_disabled_hint
                    )
                }
            } else {
                if (isFormEditableAfterFinalization) {
                    setWarning(
                        icon = R.drawable.ic_edit_24,
                        title = org.odk.collect.strings.R.string.form_editing_enabled_after_finalizing,
                        hint = org.odk.collect.strings.R.string.form_editing_enabled_after_finalizing_hint
                    )
                } else {
                    setWarning(
                        icon = R.drawable.ic_edit_off_24,
                        title = org.odk.collect.strings.R.string.form_editing_disabled_after_finalizing,
                        hint = org.odk.collect.strings.R.string.form_editing_disabled_hint
                    )
                }
            }
        } else if (binding.finalize.isVisible) {
            if (shouldFormBeSentAutomatically) {
                if (isFormEditableAfterFinalization) {
                    setWarning(
                        icon = R.drawable.ic_edit_24,
                        title = org.odk.collect.strings.R.string.form_editing_enabled_after_sending,
                        hint = org.odk.collect.strings.R.string.form_editing_enabled_after_sending_hint
                    )
                } else {
                    setWarning(
                        icon = R.drawable.ic_edit_off_24,
                        title = org.odk.collect.strings.R.string.form_editing_disabled_after_sending,
                        hint = null
                    )
                }
            } else {
                if (isFormEditableAfterFinalization) {
                    setWarning(
                        icon = R.drawable.ic_edit_24,
                        title = org.odk.collect.strings.R.string.form_editing_enabled_after_finalizing,
                        hint = org.odk.collect.strings.R.string.form_editing_enabled_after_finalizing_hint
                    )
                } else {
                    setWarning(
                        icon = R.drawable.ic_edit_off_24,
                        title = org.odk.collect.strings.R.string.form_editing_disabled_after_finalizing,
                        hint = null
                    )
                }
            }
        } else {
            binding.formEditsWarning.visibility = View.GONE
        }
    }

    private fun setWarning(icon: Int, title: Int, hint: Int?) {
        binding.formEditsIcon.setImageResource(icon)
        binding.formEditsWarningTitle.setText(title)

        if (hint != null) {
            binding.formEditsWarningMessage.setText(hint)
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
