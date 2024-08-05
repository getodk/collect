package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.widget.doOnTextChanged
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.databinding.CounterWidgetBinding
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.utilities.StringWidgetUtils

class CounterWidget(
    context: Context,
    questionDetails: QuestionDetails
) : QuestionWidget(context, questionDetails) {
    lateinit var binding: CounterWidgetBinding

    init {
        render()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateAnswerView(
        context: Context,
        prompt: FormEntryPrompt,
        answerFontSize: Int
    ): View {
        binding = CounterWidgetBinding.inflate(LayoutInflater.from(context))

        binding.value.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrBlank() || Integer.parseInt(text.toString()) == 0) {
                binding.minusButton.isEnabled = false
            } else if (Integer.parseInt(text.toString()) == 999999999) {
                binding.plusButton.isEnabled = false
            } else {
                binding.minusButton.isEnabled = true
                binding.plusButton.isEnabled = true
            }
        }

        formEntryPrompt.answerValue?.let {
            val value = Integer.parseInt(it.value.toString())
            if (value in 0..999999999) {
                binding.value.text = value.toString()
            }
        } ?: run {
            binding.value.text = null
        }

        if (formEntryPrompt.isReadOnly) {
            binding.minusButton.isEnabled = false
            binding.plusButton.isEnabled = false
        }

        binding.minusButton.setOnClickListener {
            val currentValue = Integer.parseInt(binding.value.text.toString())
            binding.value.text = (currentValue - 1).toString()
            widgetValueChanged()
        }

        binding.plusButton.setOnClickListener {
            if (binding.value.text.isNullOrBlank()) {
                binding.value.text = "1"
            } else {
                val currentValue = Integer.parseInt(binding.value.text.toString())
                binding.value.text = (currentValue + 1).toString()
            }
            widgetValueChanged()
        }
        return binding.root
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) = Unit

    override fun getAnswer(): IAnswerData? {
        return StringWidgetUtils.getIntegerData(binding.value.text.toString(), formEntryPrompt)
    }

    override fun clearAnswer() {
        binding.value.text = null
        widgetValueChanged()
    }
}
