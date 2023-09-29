package org.odk.collect.android.views

import android.content.Context
import android.text.Selection
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.odk.collect.android.databinding.WidgetAnswerTextBinding
import org.odk.collect.android.utilities.SoftKeyboardController

class WidgetAnswerText(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    private val binding = WidgetAnswerTextBinding.inflate(LayoutInflater.from(context), this, true)

    fun updateState(readOnly: Boolean) {
        if (readOnly) {
            binding.textInputLayout.visibility = GONE
            binding.textView.visibility = VISIBLE
        } else {
            binding.textInputLayout.visibility = VISIBLE
            binding.textView.visibility = GONE
        }
    }

    fun getAnswer(): String {
        return binding.editText.text.toString()
    }

    fun setAnswer(answer: String?) {
        binding.editText.setText(answer)
        binding.textView.text = answer
        Selection.setSelection(binding.editText.text, binding.editText.text.toString().length)
    }

    fun clearAnswer() {
        binding.editText.text = null
        binding.textView.text = null
    }

    fun setError(error: String?) {
        binding.textInputLayout.error = error
    }

    fun setFocus(focus: Boolean) {
        if (focus) {
            SoftKeyboardController().showSoftKeyboard(binding.editText)
        } else {
            SoftKeyboardController().hideSoftKeyboard(binding.editText)
        }
    }
}
