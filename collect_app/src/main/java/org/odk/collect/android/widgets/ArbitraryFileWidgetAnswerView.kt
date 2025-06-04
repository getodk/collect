package org.odk.collect.android.widgets

import android.content.Context
import android.view.LayoutInflater
import org.odk.collect.android.databinding.ArbitraryFileWidgetAnswerViewBinding

class ArbitraryFileWidgetAnswerView(context: Context) : WidgetAnswerView(context) {
    private val binding = ArbitraryFileWidgetAnswerViewBinding.inflate(LayoutInflater.from(context), this, true)

    override fun setAnswer(answer: String?) {
        binding.answer.text = answer
    }

    override fun getAnswer(): String {
        return binding.answer.text.toString()
    }
}
