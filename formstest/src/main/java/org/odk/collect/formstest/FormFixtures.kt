package org.odk.collect.formstest

import org.odk.collect.forms.Form
import org.odk.collect.shared.TempFiles
import java.io.File

object FormFixtures {
    // If you set the date here, it might be overridden with the current date during saving to the database if dbId is not set too
    fun form(
        formId: String = "formId",
        version: String = "1",
        formFilePath: String? = null,
        mediaFiles: List<Pair<String, String>> = emptyList(),
        autoSend: String? = null
    ): Form {
        val formFilesPath = TempFiles.createTempDir().absolutePath
        val mediaFilePath = TempFiles.createTempDir().absolutePath

        mediaFiles.forEach { (name, contents) ->
            File(mediaFilePath, name).also { it.writeBytes(contents.toByteArray()) }
        }

        return Form.Builder()
            .displayName("Test Form")
            .formId(formId)
            .version(version)
            .formFilePath(
                formFilePath ?: FormUtils.createFormFixtureFile(
                    formId,
                    version,
                    formFilesPath
                )
            )
            .formMediaPath(mediaFilePath)
            .autoSend(autoSend)
            .build()
    }
}
