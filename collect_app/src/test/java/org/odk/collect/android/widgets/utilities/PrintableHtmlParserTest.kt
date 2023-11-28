package org.odk.collect.android.widgets.utilities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.jsoup.Jsoup
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.qrcode.QRCodeCreatorImpl
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class PrintableHtmlParserTest {
    private val questionMediaManager = mock<QuestionMediaManager>()

    @Test
    fun `parsing an empty string does not crash`() {
        val printableHtmlParser = PrintableHtmlParser(QRCodeCreatorImpl())
        val parsedHtml = printableHtmlParser.parse("", questionMediaManager)

        assertThat(parsedHtml, equalTo("<html>\n <head></head>\n <body></body>\n</html>"))
    }

    @Test
    fun `parsing a broken HTML with unclosed tags does not crash`() {
        val printableHtmlParser = PrintableHtmlParser(QRCodeCreatorImpl())
        val parsedHtml = printableHtmlParser.parse(
            "<h1>This is an <b>unclosed tag<p>Broken HTML example</h1>",
            questionMediaManager
        )

        assertThat(parsedHtml, equalTo("<html>\n <head></head>\n <body>\n  <h1>This is an <b>unclosed tag\n    <p>Broken HTML example</p></b></h1>\n </body>\n</html>"))
    }

    @Test
    fun `parsing a partial HTML without html, head and body tags adds it`() {
        val printableHtmlParser = PrintableHtmlParser(QRCodeCreatorImpl())
        val parsedHtml = printableHtmlParser.parse(
            "blah",
            questionMediaManager
        )

        assertThat(parsedHtml, equalTo("<html>\n <head></head>\n <body>\n  blah\n </body>\n</html>"))
    }

    @Test
    fun `parsing an HTML with css keeps it`() {
        val printableHtmlParser = PrintableHtmlParser(QRCodeCreatorImpl())
        val parsedHtml = printableHtmlParser.parse(
            "<html lang=\"en\"> <head> <meta charset=\"UTF-8\"> <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"> <title>Embedded CSS Example</title> <style> body { font-family: 'Arial', sans-serif; background-color: #f0f0f0; } h1 { color: blue; } p { font-size: 16px; line-height: 1.5; } </style> </head> <body> <h1>Hello, World!</h1> <p>This is a sample HTML document with embedded CSS.</p> </body> </html>",
            questionMediaManager
        )

        assertThat(parsedHtml, equalTo("<html lang=\"en\">\n <head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>Embedded CSS Example</title>\n  <style> body { font-family: 'Arial', sans-serif; background-color: #f0f0f0; } h1 { color: blue; } p { font-size: 16px; line-height: 1.5; } </style>\n </head>\n <body>\n  <h1>Hello, World!</h1>\n  <p>This is a sample HTML document with embedded CSS.</p>\n </body>\n</html>"))
    }

    @Test
    fun `parsing an HTML with images sets the src attribute to data urls`() {
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).also {
            it.drawColor(Color.WHITE)
        }
        val tempFile = File.createTempFile("photo", ".png")

        val fos = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        whenever(questionMediaManager.getAnswerFile("photo.png")).thenReturn(tempFile)

        val printableHtmlParser = PrintableHtmlParser(QRCodeCreatorImpl())
        val parsedHtml = printableHtmlParser.parse(
            "<img width=\"150\" height=\"150\" src=\"photo.png\">",
            questionMediaManager
        )

        assertThat(parsedHtml, equalTo("<html>\n <head></head>\n <body>\n  <img width=\"150\" height=\"150\" src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAEElEQVR4AQEFAPr/AAAAAAAABQAB\nZHiVOAAAAABJRU5ErkJggg==\n\">\n </body>\n</html>"))
    }

    @Test
    fun `parsing an HTML with a non-existing image does not modify the src attribute`() {
        whenever(questionMediaManager.getAnswerFile("photo.png")).thenReturn(null)

        val printableHtmlParser = PrintableHtmlParser(QRCodeCreatorImpl())
        val parsedHtml = printableHtmlParser.parse(
            "<img width=\"150\" height=\"150\" src=\"photo.png\">",
            questionMediaManager
        )

        assertThat(parsedHtml, equalTo("<html>\n <head></head>\n <body>\n  <img width=\"150\" height=\"150\" src=\"photo.png\">\n </body>\n</html>"))
    }

    @Test
    fun `parsing an HTML converts qrcode tags to img ones`() {
        val printableHtmlParser = PrintableHtmlParser(QRCodeCreatorImpl())
        val parsedHtml = printableHtmlParser.parse(
            "<qrcode width=\"150\" height=\"150\">blah</qrcode>",
            questionMediaManager
        )
        val document = Jsoup.parse(parsedHtml)

        val imgElements = document.getElementsByTag("img")

        assertThat(document.getElementsByTag("qrcode").size, equalTo(0))

        assertThat(imgElements[0].attributes().get("src").startsWith("data:image/png;base64"), equalTo(true))
        assertThat(imgElements[0].attributes().get("width"), equalTo("150"))
        assertThat(imgElements[0].attributes().get("height"), equalTo("150"))
    }
}
