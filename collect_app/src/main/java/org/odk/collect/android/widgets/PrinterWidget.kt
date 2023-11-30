package org.odk.collect.android.widgets

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.R
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.utilities.PrintableHtmlParser
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.async.Scheduler
import org.odk.collect.material.MaterialProgressDialogFragment
import org.odk.collect.printer.HtmlPrinter

class PrinterWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val scheduler: Scheduler,
    private val questionMediaManager: QuestionMediaManager,
    private val printableHtmlParser: PrintableHtmlParser,
    private val htmlPrinter: HtmlPrinter
) : QuestionWidget(context, questionDetails) {

    private val viewModel = ViewModelProvider(getContext() as ComponentActivity)[PrinterWidgetViewModel::class.java]

    init {
        render()

        MaterialProgressDialogFragment.showOn(
            context as AppCompatActivity,
            viewModel.isLoading,
            context.supportFragmentManager
        ) {
            MaterialProgressDialogFragment().also { dialog ->
                dialog.message = context.getString(org.odk.collect.strings.R.string.loading)
            }
        }
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
            scheduler.immediate(
                background = {
                    viewModel.isLoading.postValue(true)
                    printableHtmlParser.parse(it, questionMediaManager)
                },
                foreground = { content ->
                    htmlPrinter.print(context, content)
                    viewModel.isLoading.value = false
                }
            )
        }
    }
}

class PrinterWidgetViewModel : ViewModel() {
    val isLoading = MutableNonNullLiveData(false)
}
