package org.odk.collect.android.views

import android.content.Context
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.Selection
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.odk.collect.android.databinding.WidgetAnswerTextBinding
import org.odk.collect.android.listeners.ThousandsSeparatorTextWatcher
import org.odk.collect.android.utilities.SoftKeyboardController
import java.text.NumberFormat
import java.util.Locale

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

    fun setIntegerType(useThousandSeparator: Boolean, answer: Int?) {
        if (useThousandSeparator) {
            binding.editText.addTextChangedListener(ThousandsSeparatorTextWatcher(binding.editText))
        }

        binding.editText.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED
        binding.editText.keyListener = DigitsKeyListener(true, false) // only allows numbers and no periods

        // ints can only hold 2,147,483,648. we allow 999,999,999
        val fa = arrayOfNulls<InputFilter>(1)
        fa[0] = LengthFilter(9)
        if (useThousandSeparator) {
            //11 since for a nine digit number , their will be 2 separators.
            fa[0] = LengthFilter(11)
        }
        binding.editText.filters = fa

        if (answer != null) {
            binding.editText.setText(String.format(Locale.US, "%d", answer))
            Selection.setSelection(binding.editText.text, binding.editText.text.toString().length)
        }
    }

    fun setDecimalType(useThousandSeparator: Boolean, answer: Double?) {
        if (useThousandSeparator) {
            binding.editText.addTextChangedListener(ThousandsSeparatorTextWatcher(binding.editText))
        }

        binding.editText.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        binding.editText.keyListener = DigitsKeyListener(true, true) // only numbers are allowed

        // only 15 characters allowed
        val fa = arrayOfNulls<InputFilter>(1)
        fa[0] = LengthFilter(15)
        if (useThousandSeparator) {
            fa[0] = LengthFilter(19)
        }
        binding.editText.filters = fa

        if (answer != null) {
            // truncate to 15 digits max in US locale use US locale because DigitsKeyListener can't be localized before API 26
            val nf = NumberFormat.getNumberInstance(Locale.US)
            nf.maximumFractionDigits = 15
            nf.maximumIntegerDigits = 15
            nf.isGroupingUsed = false
            val formattedValue: String = nf.format(answer)
            binding.editText.setText(formattedValue)
            Selection.setSelection(binding.editText.text, binding.editText.text.toString().length)
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
