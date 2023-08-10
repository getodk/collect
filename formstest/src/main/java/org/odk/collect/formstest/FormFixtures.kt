package org.odk.collect.formstest

import org.odk.collect.forms.Form
import org.odk.collect.shared.TempFiles
import java.io.File

object FormFixtures {
    // If you set the date here, it might be overridden with the current date during saving to the database if dbId is not set too
    fun form(
        formId: String = "formId",
        version: String = "1",
        mediaFiles: List<Pair<String, String>> = emptyList(),
        autoSend: String? = null,
        formFile: File? = null
    ): Form {
        val formFilesPath = TempFiles.createTempDir().absolutePath
        val mediaFilesPath = TempFiles.createTempDir().absolutePath

        mediaFiles.forEach { (name, contents) ->
            File(mediaFilesPath, name).also { it.writeBytes(contents.toByteArray()) }
        }

        return Form.Builder()
            .displayName("Test Form")
            .formId(formId)
            .version(version)
            .formFilePath(
                formFile?.absolutePath ?: FormUtils.createFormFixtureFile(
                    formId,
                    version,
                    formFilesPath
                )
            )
            .formMediaPath(mediaFilesPath)
            .autoSend(autoSend)
            .build()
    }
}
