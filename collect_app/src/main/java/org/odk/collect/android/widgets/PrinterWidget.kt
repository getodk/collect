package org.odk.collect.android.widgets

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.utilities.PrintableHtmlParser
import org.odk.collect.printer.HtmlPrinter

class PrinterWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val questionMediaManager: QuestionMediaManager,
    private val printableHtmlParser: PrintableHtmlParser,
    private val htmlPrinter: HtmlPrinter
) : QuestionWidget(context, questionDetails) {
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

    override fun registerToClearAnswerOnLongPress(activity: Activity?, viewGroup: ViewGroup?) = Unit

    private fun print() {
        formEntryPrompt.answerText?.let {
            val content = printableHtmlParser.parse(it, questionMediaManager)
            htmlPrinter.print(context, content)
        }
    }
}
