package org.odk.collect.android.javarosawrapper

import org.javarosa.xform.parse.XFormParser
import org.kxml2.kdom.Document
import org.kxml2.kdom.Element
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader

object XFormParser {
    @JvmStatic
    @Throws(FileNotFoundException::class, XmlPullParserException::class)
    fun parseXml(formFile: File): Document {
        return parseXml(formFile.inputStream())
    }

    @JvmStatic
    @Throws(XmlPullParserException::class)
    fun parseXml(formInputStream: InputStream): Document {
        return XFormParser.getXMLDocument(InputStreamReader(formInputStream))
    }
}

fun Document.getHead(): Element {
    return getRootElement().getElement(null, "head")
}

fun Document.getTitle(): String {
    return getHead().getElement(null, "title").getChild(0).toString()
}

fun Document.getModel(): Element {
    return getHead().getElement(null, "model")
}
