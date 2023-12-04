package org.odk.collect.android.widgets

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.odk.collect.android.R
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.async.Scheduler
import org.odk.collect.material.MaterialProgressDialogFragment
import org.odk.collect.printer.HtmlPrinter
import org.odk.collect.qrcode.QRCodeCreator
import java.io.ByteArrayOutputStream

class PrinterWidget(
    context: Context,
    questionDetails: QuestionDetails,
    private val scheduler: Scheduler,
    private val questionMediaManager: QuestionMediaManager,
    private val qrCodeCreator: QRCodeCreator,
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
            viewModel.parseAndPrint(scheduler, it, questionMediaManager, qrCodeCreator, context, htmlPrinter)
        }
    }
}

class PrinterWidgetViewModel : ViewModel() {
    private val _isLoading = MutableNonNullLiveData(false)
    val isLoading: NonNullLiveData<Boolean> = _isLoading

    fun parseAndPrint(
        scheduler: Scheduler,
        htmlDocument: String,
        questionMediaManager: QuestionMediaManager,
        qrCodeCreator: QRCodeCreator,
        context: Context,
        htmlPrinter: HtmlPrinter
    ) {
        scheduler.immediate(
            background = {
                _isLoading.postValue(true)

                val document = Jsoup.parse(htmlDocument)

                parseImages(document, questionMediaManager)
                parseQRCodes(document, qrCodeCreator)

                document.html()
            },
            foreground = { content ->
                htmlPrinter.print(context, content)
                _isLoading.value = false
            }
        )
    }

    private fun parseImages(document: Document, questionMediaManager: QuestionMediaManager) {
        for (imgElement in document.getElementsByTag("img")) {
            val file = questionMediaManager.getAnswerFile(imgElement.attributes().get("src"))
            if (file != null && file.exists()) {
                imgElement.attr("src", "file://${file.absolutePath}")
            }
        }
    }

    private fun parseQRCodes(document: Document, qrCodeCreator: QRCodeCreator) {
        for (qrcodeElement in document.getElementsByTag("qrcode")) {
            val newElement = document.createElement("img").apply {
                attributes().addAll(qrcodeElement.attributes())
                val qrCodeData = bitmapToBase64(qrCodeCreator.create(qrcodeElement.text()))
                attr("src", "data:image/png;base64,$qrCodeData")
            }
            qrcodeElement.replaceWith(newElement)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
