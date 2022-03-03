package org.odk.collect.android.openrosa

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.kxml2.io.KXmlParser
import org.kxml2.kdom.Document
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.lang.StringBuilder

class OpenRosaResponseParserImplTest {

    @Test
    fun `when xform hash is missing prefix, parseFormList returns null hash for item`() {
        val response = StringBuilder()
            .appendLine("<?xml version='1.0' encoding='UTF-8' ?>")
            .appendLine("<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">")
            .appendLine("<xform>")
            .appendLine("<formID>id</formID>")
            .appendLine("<name>form name</name>")
            .appendLine("<version>1</version>")
            .appendLine("<hash>blahblah</hash>")
            .appendLine("<downloadUrl>http://example.com</downloadUrl>")
            .appendLine("</xform>")
            .appendLine("</xforms>")
            .toString()

        val doc = StringReader(response).use { reader ->
            val parser = KXmlParser()
            parser.setInput(reader)
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            Document().also { it.parse(parser) }
        }

        val formList = OpenRosaResponseParserImpl().parseFormList(doc)
        assertThat(formList!![0].hash, equalTo(null))
    }

    @Test
    fun `when media file hash is empty, parseManifest returns null`() {
        val response = StringBuilder()
            .appendLine("<?xml version='1.0' encoding='UTF-8' ?>")
            .appendLine("<manifest xmlns=\"http://openrosa.org/xforms/xformsManifest\">")
            .appendLine("<mediaFile>")
            .appendLine("<filename>badger.png</filename>")
            .appendLine("<hash></hash>")
            .appendLine("<downloadUrl>http://funk.appspot.com/binaryData?blobKey=%3A477e3</downloadUrl>")
            .appendLine("</mediaFile>")
            .appendLine("</manifest>")
            .toString()

        val doc = StringReader(response).use { reader ->
            val parser = KXmlParser()
            parser.setInput(reader)
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            Document().also { it.parse(parser) }
        }

        val mediaFiles = OpenRosaResponseParserImpl().parseManifest(doc)
        assertThat(mediaFiles, equalTo(null))
    }
}
