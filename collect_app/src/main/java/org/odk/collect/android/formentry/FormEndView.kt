package org.odk.collect.android.formentry

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import androidx.core.text.color
import androidx.core.text.inSpans
import androidx.core.text.underline
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import org.odk.collect.android.R
import org.odk.collect.android.activities.WebViewActivity
import org.odk.collect.android.databinding.FormEntryEndBinding
import org.odk.collect.androidshared.system.ContextUtils
import org.odk.collect.strings.localization.getLocalizedString

class FormEndView(
    context: Context,
    formTitle: String,
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

        val shouldFormBeSentAutomatically = formEndViewModel.shouldFormBeSentAutomatically()
        if (shouldFormBeSentAutomatically) {
            binding.finalize.text = context.getString(org.odk.collect.strings.R.string.send)
        }

        if (!binding.saveAsDraft.isVisible && !shouldFormBeSentAutomatically) {
            binding.formEditsWarningTitle.setText(org.odk.collect.strings.R.string.form_edits_warning_title)
            binding.formEditsWarningMessage.apply {
                text = getLearnMoreLink()
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = Color.TRANSPARENT
            }
        } else if (binding.saveAsDraft.isVisible && binding.finalize.isVisible) {
            if (shouldFormBeSentAutomatically) {
                binding.formEditsWarningTitle.setText(org.odk.collect.strings.R.string.form_edits_warning_title_auto_send_enabled)
            } else {
                binding.formEditsWarningTitle.setText(org.odk.collect.strings.R.string.form_edits_warning_title)
            }
            binding.formEditsWarningMessage.apply {
                text = SpannableStringBuilder()
                    .append(context.getLocalizedString(org.odk.collect.strings.R.string.form_edits_warning_message))
                    .append(" ")
                    .append(getLearnMoreLink())
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = Color.TRANSPARENT
            }
        } else {
            binding.formEditsWarning.visibility = View.GONE
        }
    }

    private fun getLearnMoreLink(): SpannableStringBuilder {
        return SpannableStringBuilder().inSpans(
            span = object : ClickableSpan() {
                override fun onClick(view: View) {
                    val intent = Intent(context, WebViewActivity::class.java)
                    intent.putExtra("url", "https://forum.getodk.org/t/42007")
                    context.startActivity(intent)
                }
            },
            builderAction = {
                inSpans(
                    span = TextAppearanceSpan(context, com.google.android.material.R.style.TextAppearance_Material3_TitleMedium),
                    builderAction = {
                        color(ContextUtils.getThemeAttributeValue(context, com.google.android.material.R.attr.colorAccent)) {
                            underline {
                                append(context.getLocalizedString(org.odk.collect.strings.R.string.form_edits_warning_learn_more))
                            }
                        }
                    }
                )
            }
        )
    }

    override fun shouldSuppressFlingGesture() = false

    override fun verticalScrollView(): NestedScrollView? {
        return findViewById(R.id.scroll_view)
    }

    interface Listener {
        fun onSaveClicked(markAsFinalized: Boolean)
    }
}
