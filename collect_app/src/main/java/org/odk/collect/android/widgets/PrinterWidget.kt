package org.odk.collect.android.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.button.MaterialButton
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.formentry.questions.QuestionDetails

class PrinterWidget(context: Context, questionDetails: QuestionDetails) : QuestionWidget(context, questionDetails) {
    init {
        render()
    }

    override fun onCreateAnswerView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        val answerView = LayoutInflater.from(context).inflate(R.layout.printer_widget, null)
        answerView
            .findViewById<MaterialButton>(R.id.printer_button)
            .setOnClickListener {
                print()
            }
        return answerView
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) = Unit

    override fun getAnswer(): IAnswerData? = null

    override fun clearAnswer() = Unit

    private fun print() = Unit
}
