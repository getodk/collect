package org.odk.collect.android.widgets

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import org.odk.collect.android.databinding.ArbitraryFileWidgetAnswerViewBinding

class ArbitraryFileWidgetAnswerView(
    context: Context,
    private val fontSize: Int
) : WidgetAnswerView(context) {
    private val binding = ArbitraryFileWidgetAnswerViewBinding.inflate(LayoutInflater.from(context), this, true)
    private var answer: String? = null

    init {
        setFontSize()
    }

    override fun setAnswer(answer: String?) {
        this.answer = answer
        binding.answer.text = answer
    }

    override fun getAnswer(): String? {
        return answer
    }

    override fun setFontSize() {
        binding.answer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize.toFloat())
    }
}
