package org.odk.collect.android.widgets.utilities

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.jsoup.Jsoup
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.qrcode.QRCodeCreatorImpl

@RunWith(AndroidJUnit4::class)
class PrintableHtmlParserTest {
    @Test
    fun `parsing an empty string does not crash`() {
        val printableHtmlParser = PrintableHtmlParser(QRCodeCreatorImpl())
        val parsedHtml = printableHtmlParser.parse("")

        assertThat(parsedHtml, equalTo("<html>\n <head></head>\n <body></body>\n</html>"))
    }

    @Test
    fun `parsing a broken HTML with unclosed tags does not crash`() {
        val printableHtmlParser = PrintableHtmlParser(QRCodeCreatorImpl())
        val parsedHtml = printableHtmlParser.parse(
            "<h1>This is an <b>unclosed tag<p>Broken HTML example</h1>"
        )

        assertThat(parsedHtml, equalTo("<html>\n <head></head>\n <body>\n  <h1>This is an <b>unclosed tag\n    <p>Broken HTML example</p></b></h1>\n </body>\n</html>"))
    }

    @Test
    fun `parsing a partial HTML without html, head and body tags adds it`() {
        val printableHtmlParser = PrintableHtmlParser(QRCodeCreatorImpl())
        val parsedHtml = printableHtmlParser.parse(
            "blah"
        )

        assertThat(parsedHtml, equalTo("<html>\n <head></head>\n <body>\n  blah\n </body>\n</html>"))
    }

    @Test
    fun `parsing an HTML with css keeps it`() {
        val printableHtmlParser = PrintableHtmlParser(QRCodeCreatorImpl())
        val parsedHtml = printableHtmlParser.parse(
            "<html lang=\"en\"> <head> <meta charset=\"UTF-8\"> <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"> <title>Embedded CSS Example</title> <style> body { font-family: 'Arial', sans-serif; background-color: #f0f0f0; } h1 { color: blue; } p { font-size: 16px; line-height: 1.5; } </style> </head> <body> <h1>Hello, World!</h1> <p>This is a sample HTML document with embedded CSS.</p> </body> </html>"
        )

        assertThat(parsedHtml, equalTo("<html lang=\"en\">\n <head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>Embedded CSS Example</title>\n  <style> body { font-family: 'Arial', sans-serif; background-color: #f0f0f0; } h1 { color: blue; } p { font-size: 16px; line-height: 1.5; } </style>\n </head>\n <body>\n  <h1>Hello, World!</h1>\n  <p>This is a sample HTML document with embedded CSS.</p>\n </body>\n</html>"))
    }

    @Test
    fun `parsing an HTML converts qrcode tags to img ones`() {
        val printableHtmlParser = PrintableHtmlParser(QRCodeCreatorImpl())
        val parsedHtml = printableHtmlParser.parse("<qrcode width=\"150\" height=\"150\">blah</qrcode>")
        val document = Jsoup.parse(parsedHtml)

        val imgElements = document.getElementsByTag("img")

        assertThat(document.getElementsByTag("qrcode").size, equalTo(0))

        assertThat(imgElements[0].attributes().get("src").startsWith("data:image/png;base64"), equalTo(true))
        assertThat(imgElements[0].attributes().get("width"), equalTo("150"))
        assertThat(imgElements[0].attributes().get("height"), equalTo("150"))
    }
}
