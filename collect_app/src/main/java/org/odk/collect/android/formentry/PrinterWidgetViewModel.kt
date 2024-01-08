package org.odk.collect.android.formentry

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.interfaces.Printer
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.async.Scheduler
import org.odk.collect.printer.HtmlPrinter
import org.odk.collect.qrcode.QRCodeCreator
import java.io.ByteArrayOutputStream

class PrinterWidgetViewModel(
    private val scheduler: Scheduler,
    private val qrCodeCreator: QRCodeCreator,
    private val htmlPrinter: HtmlPrinter
) : ViewModel(), Printer {
    private val _isLoading = MutableNonNullLiveData(false)

    @Override
    override fun parseAndPrint(
        htmlDocument: String,
        questionMediaManager: QuestionMediaManager,
        context: Context
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

    override fun isLoading(): NonNullLiveData<Boolean> {
        return _isLoading
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
