package org.odk.collect.android.widgets.utilities

import android.graphics.Bitmap
import android.util.Base64
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.odk.collect.qrcode.QRCodeCreator
import java.io.ByteArrayOutputStream

class PrintableHtmlParser(private val qrCodeCreator: QRCodeCreator) {
    fun parse(htmlDocument: String): String {
        val document = Jsoup.parse(htmlDocument)

        return convertQRCodeElementsToImages(document).html()
    }

    private fun convertQRCodeElementsToImages(document: Document): Document {
        for (qrcodeElement in document.getElementsByTag("qrcode")) {
            val newElement = document.createElement("img").apply {
                attributes().addAll(qrcodeElement.attributes())
                val qrCodeData = bitmapToBase64(qrCodeCreator.create(qrcodeElement.text()))
                attr("src", "data:image/png;base64,$qrCodeData")
            }
            qrcodeElement.replaceWith(newElement)
        }
        return document
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
