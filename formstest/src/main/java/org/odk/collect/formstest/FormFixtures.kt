package org.odk.collect.formstest

import org.odk.collect.forms.Form
import org.odk.collect.shared.TempFiles
import java.io.File

object FormFixtures {
    fun form(
        formId: String = "formId",
        version: String = "1",
        date: Long = 1,
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
            .formFilePath(FormUtils.createFormFixtureFile(formId, version, formFilesPath))
            .formMediaPath(mediaFilePath)
            .date(date)
            .autoSend(autoSend)
            .build()
    }
}
