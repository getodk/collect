package org.odk.collect.formstest

import org.apache.commons.io.FileUtils
import org.odk.collect.forms.Form
import java.io.File
import java.io.IOException
import java.nio.charset.Charset

object FormUtils {
    @JvmStatic
    @JvmOverloads
    fun createXFormBody(formId: String, version: String?, title: String = "Test Form", entitiesVersion: String? = null): String {
        val entitiesVersionAttribute = entitiesVersion?.let { """entities:entities-version="$it"""" } ?: ""

        return """<?xml version="1.0"?>
                    <h:html xmlns="http://www.w3.org/2002/xforms" xmlns:ev="http://www.w3.org/2001/xml-events" xmlns:h="http://www.w3.org/1999/xhtml" xmlns:jr="http://openrosa.org/javarosa" xmlns:orx="http://openrosa.org/xforms" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:entities="http://www.opendatakit.org/xforms/entities">
                        <h:head>
                            <h:title>$title</h:title>
                            <model $entitiesVersionAttribute>
                                <instance>
                                    <data id="$formId" orx:version="$version">
                                        <question/>
                                    </data>
                                </instance>
                                <bind nodeset="/data/question" type="string"/>
                            </model>
                        </h:head>
                        <h:body>
                            <input ref="/data/question">
                                <label>question label</label>
                            </input>
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
        autosend: String? = null,
        usesEntities: Boolean = false
    ): Form.Builder {
        val formFilePath = createFormFixtureFile(formId, version, formFilesPath, xform)

        return Form.Builder()
            .displayName("Test Form")
            .formId(formId)
            .version(version)
            .formFilePath(formFilePath)
            .autoSend(autosend)
            .usesEntities(usesEntities)
    }

    fun createFormFixtureFile(
        formId: String,
        version: String?,
        formFilesPath: String,
        xform: String = createXFormBody(formId, version)
    ): String {
        val fileName = formId + "-" + version + "-" + Math.random()
        val formFile = File("$formFilesPath/$fileName.xml")

        try {
            FileUtils.writeByteArrayToFile(formFile, xform.toByteArray())
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return formFile.absolutePath
    }
}
