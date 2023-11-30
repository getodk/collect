package org.odk.collect.android.widgets.utilities

import android.graphics.Bitmap
import android.util.Base64
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.qrcode.QRCodeCreator
import java.io.ByteArrayOutputStream

class PrintableHtmlParser(private val qrCodeCreator: QRCodeCreator) {
    fun parse(htmlDocument: String, questionMediaManager: QuestionMediaManager): String {
        val document = Jsoup.parse(htmlDocument)

        return convertQRCodeElementsToImages(document, questionMediaManager).html()
    }

    private fun convertQRCodeElementsToImages(document: Document, questionMediaManager: QuestionMediaManager): Document {
        parseImages(document, questionMediaManager)
        parseQRCodes(document)

        return document
    }

    private fun parseImages(document: Document, questionMediaManager: QuestionMediaManager) {
        for (imgElement in document.getElementsByTag("img")) {
            val file = questionMediaManager.getAnswerFile(imgElement.attributes().get("src"))
            if (file != null && file.exists()) {
                imgElement.attr("src", "file://${file.absolutePath}")
            }
        }
    }

    private fun parseQRCodes(document: Document) {
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
