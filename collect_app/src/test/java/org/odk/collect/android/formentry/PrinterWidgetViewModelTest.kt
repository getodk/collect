package org.odk.collect.android.formentry

import android.content.Context
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.printer.HtmlPrinter
import org.odk.collect.qrcode.QRCodeCreator
import org.odk.collect.testshared.FakeScheduler
import java.io.File

@RunWith(AndroidJUnit4::class)
class PrinterWidgetViewModelTest {
    private val scheduler = FakeScheduler()
    private val questionMediaManager = mock<QuestionMediaManager>()
    private val qrCodeCreator = mock<QRCodeCreator>()
    private val context = mock<Context>()
    private val htmlPrinter = mock<HtmlPrinter>()

    @Test
    fun `printing an empty string does not crash`() {
        val printerWidgetViewModel = PrinterWidgetViewModel(scheduler, qrCodeCreator, htmlPrinter)
        val html = ""
        printerWidgetViewModel.parseAndPrint(html, questionMediaManager, context)
        scheduler.runBackground()
        scheduler.runForeground()

        verify(htmlPrinter).print(
            context,
            "<html>\n <head></head>\n <body></body>\n</html>"
        )
    }

    @Test
    fun `parsing a broken HTML with unclosed tags does not crash`() {
        val printerWidgetViewModel = PrinterWidgetViewModel(scheduler, qrCodeCreator, htmlPrinter)
        val html = "<h1>This is an <b>unclosed tag<p>Broken HTML example</h1>"
        printerWidgetViewModel.parseAndPrint(html, questionMediaManager, context)
        scheduler.runBackground()
        scheduler.runForeground()

        verify(htmlPrinter).print(
            context,
            "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <h1>This is an <b>unclosed tag\n" +
                "    <p>Broken HTML example</p></b></h1>\n" +
                " </body>\n" +
                "</html>"
        )
    }

    @Test
    fun `printing a partial HTML without html, head and body tags adds it`() {
        val printerWidgetViewModel = PrinterWidgetViewModel(scheduler, qrCodeCreator, htmlPrinter)
        val html = "blah"
        printerWidgetViewModel.parseAndPrint(html, questionMediaManager, context)
        scheduler.runBackground()
        scheduler.runForeground()

        verify(htmlPrinter).print(
            context,
            "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  blah\n" +
                " </body>\n" +
                "</html>"
        )
    }

    @Test
    fun `printing an HTML with css keeps it`() {
        val printerWidgetViewModel = PrinterWidgetViewModel(scheduler, qrCodeCreator, htmlPrinter)
        val html = "<html lang=\"en\"> <head> <meta charset=\"UTF-8\"> <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"> <title>Embedded CSS Example</title> <style> body { font-family: 'Arial', sans-serif; background-color: #f0f0f0; } h1 { color: blue; } p { font-size: 16px; line-height: 1.5; } </style> </head> <body> <h1>Hello, World!</h1> <p>This is a sample HTML document with embedded CSS.</p> </body> </html>"
        printerWidgetViewModel.parseAndPrint(html, questionMediaManager, context)
        scheduler.runBackground()
        scheduler.runForeground()

        verify(htmlPrinter).print(
            context,
            "<html lang=\"en\">\n" +
                " <head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>Embedded CSS Example</title>\n" +
                "  <style> body { font-family: 'Arial', sans-serif; background-color: #f0f0f0; } h1 { color: blue; } p { font-size: 16px; line-height: 1.5; } </style>\n" +
                " </head>\n" +
                " <body>\n" +
                "  <h1>Hello, World!</h1>\n" +
                "  <p>This is a sample HTML document with embedded CSS.</p>\n" +
                " </body>\n" +
                "</html>"
        )
    }

    @Test
    fun `printing an HTML with images sets the src attribute to an absolute path of an image`() {
        val tempFile = File.createTempFile("photo", ".png")
        whenever(questionMediaManager.getAnswerFile("photo.png")).thenReturn(tempFile)
        val printerWidgetViewModel = PrinterWidgetViewModel(scheduler, qrCodeCreator, htmlPrinter)
        val html = "<img width=\"150\" height=\"150\" src=\"photo.png\">"
        printerWidgetViewModel.parseAndPrint(html, questionMediaManager, context)
        scheduler.runBackground()
        scheduler.runForeground()

        verify(htmlPrinter).print(
            context,
            "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <img width=\"150\" height=\"150\" src=\"file://${tempFile.absolutePath}\">\n" +
                " </body>\n" +
                "</html>"
        )
    }

    @Test
    fun `printing an HTML with a non-existing image does not modify the src attribute`() {
        whenever(questionMediaManager.getAnswerFile("photo.png")).thenReturn(null)
        val printerWidgetViewModel = PrinterWidgetViewModel(scheduler, qrCodeCreator, htmlPrinter)
        val html = "<img width=\"150\" height=\"150\" src=\"photo.png\">"
        printerWidgetViewModel.parseAndPrint(html, questionMediaManager, context)
        scheduler.runBackground()
        scheduler.runForeground()

        verify(htmlPrinter).print(
            context,
            "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <img width=\"150\" height=\"150\" src=\"photo.png\">\n" +
                " </body>\n" +
                "</html>"
        )
    }

    @Test
    fun `printing an HTML converts qrcode tags to img ones`() {
        whenever(qrCodeCreator.create(any())).thenReturn(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
        val printerWidgetViewModel = PrinterWidgetViewModel(scheduler, qrCodeCreator, htmlPrinter)
        val html = "<qrcode width=\"150\" height=\"150\">blah</qrcode>"
        printerWidgetViewModel.parseAndPrint(html, questionMediaManager, context)
        scheduler.runBackground()
        scheduler.runForeground()

        verify(htmlPrinter).print(
            context,
            "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <img width=\"150\" height=\"150\" src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAEElEQVR4AQEFAPr/AAAAAAAABQAB\n" +
                "ZHiVOAAAAABJRU5ErkJggg==\n" +
                "\">\n" +
                " </body>\n" +
                "</html>"
        )
    }
}
