package org.odk.collect.formstest

import org.apache.commons.io.FileUtils
import org.odk.collect.forms.Form
import java.io.File
import java.io.IOException
import java.lang.RuntimeException
import java.nio.charset.Charset

object FormUtils {
    @JvmStatic
    @JvmOverloads
    fun createXFormBody(formId: String, version: String?, title: String = "Form"): String {
        return """<?xml version="1.0"?>
                    <h:html xmlns="http://www.w3.org/2002/xforms" xmlns:ev="http://www.w3.org/2001/xml-events" xmlns:h="http://www.w3.org/1999/xhtml" xmlns:jr="http://openrosa.org/javarosa" xmlns:orx="http://openrosa.org/xforms" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
                        <h:head>
                            <h:title>$title</h:title>
                            <model>
                                <instance>
                                    <data id="$formId" orx:version="$version">
                                    </data>
                                </instance>
                            </model>
                        </h:head>
                        <h:body>
                        </h:body>
                    </h:html>"""
    }

    @JvmStatic
    @JvmOverloads
    fun createXFormFile(formId: String, version: String?, title: String = "Form"): File {
        val body = createXFormBody(formId, version, title)
        return try {
            val file = File.createTempFile("$formId-$version", ".xml")
            FileUtils.writeStringToFile(file, body, Charset.defaultCharset())
            file
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun buildForm(
        formId: String,
        version: String?,
        formFilesPath: String,
        xform: String = createXFormBody(formId, version),
        autosend: String? = null
    ): Form.Builder {
        val fileName = formId + "-" + version + "-" + Math.random()
        val formFile = File("$formFilesPath/$fileName.xml")

        try {
            FileUtils.writeByteArrayToFile(formFile, xform.toByteArray())
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return Form.Builder()
            .displayName("Test Form")
            .formFilePath(formFile.absolutePath)
            .formId(formId)
            .version(version)
            .autoSend(autosend)
    }
}
