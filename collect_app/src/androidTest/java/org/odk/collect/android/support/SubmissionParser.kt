package org.odk.collect.android.support

import org.javarosa.xform.parse.XFormParser
import java.io.File

object SubmissionParser {
    fun getMetaIds(file: File): Pair<String, String?> {
        val formRootElement = XFormParser.getXMLDocument(file.inputStream().reader()).rootElement
        val formMetaElement = formRootElement.getElement(null, "meta")
        val instanceID = formMetaElement.getElement(null, "instanceID").getText(0)
        val deprecatedID = try {
            formMetaElement.getElement(null, "deprecatedID").getText(0)
        } catch (e: Exception) {
            null
        }

        return Pair(instanceID, deprecatedID)
    }
}
