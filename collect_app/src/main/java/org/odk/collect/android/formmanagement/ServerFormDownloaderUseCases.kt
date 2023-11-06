package org.odk.collect.android.formmanagement

import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.FileUtils.LAST_SAVED_FILENAME
import org.odk.collect.forms.FormsRepository
import java.io.File

object ServerFormDownloaderUseCases {
    @JvmStatic
    fun copySavedFileFromPreviousFormVersionIfExists(formsRepository: FormsRepository, formId: String, mediaDirPath: String) {
        val lastSavedFile: File? = formsRepository
            .getAllByFormId(formId)
            .maxByOrNull { form -> form.date }
            ?.let {
                File(it.formMediaPath, LAST_SAVED_FILENAME)
            }

        if (lastSavedFile != null && lastSavedFile.exists()) {
            File(mediaDirPath).mkdir()
            FileUtils.copyFile(lastSavedFile, File(mediaDirPath, LAST_SAVED_FILENAME))
        }
    }
}
