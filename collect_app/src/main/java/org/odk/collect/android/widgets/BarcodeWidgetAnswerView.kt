package org.odk.collect.android.widgets

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import org.odk.collect.android.databinding.BarcodeWidgetAnswerViewBinding
import org.odk.collect.android.widgets.utilities.QuestionFontSizeUtils
import org.odk.collect.settings.SettingsProvider

class BarcodeWidgetAnswerView(
    context: Context,
    private val settingsProvider: SettingsProvider
) : WidgetAnswerView(context) {
    private val binding = BarcodeWidgetAnswerViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setFontSize()
    }

    override fun setAnswer(answer: String?) {
        binding.answer.text = stripInvalidCharacters(answer)
    }

    override fun getAnswer(): String {
        return binding.answer.text.toString()
    }

    override fun setFontSize() {
        val textSize = QuestionFontSizeUtils.getFontSize(
            settingsProvider.getUnprotectedSettings(),
            QuestionFontSizeUtils.FontSize.HEADLINE_6
        )
        binding.answer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize.toFloat())
    }

    // Remove control characters, invisible characters and unused code points.
    private fun stripInvalidCharacters(data: String?): String? {
        return data?.replace("\\p{C}".toRegex(), "")
    }
}
