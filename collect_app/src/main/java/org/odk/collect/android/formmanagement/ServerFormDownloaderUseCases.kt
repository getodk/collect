package org.odk.collect.android.formmanagement

import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.FileUtils.LAST_SAVED_FILENAME
import org.odk.collect.async.OngoingWorkListener
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.MediaFile
import org.odk.collect.shared.strings.Md5
import java.io.File
import java.io.IOException

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

    @JvmStatic
    @JvmOverloads
    @Throws(IOException::class, FormSourceException::class, InterruptedException::class)
    fun downloadMediaFiles(
        formToDownload: ServerFormDetails,
        formSource: FormSource,
        formsRepository: FormsRepository,
        tempMediaPath: String,
        tempDir: File,
        stateListener: OngoingWorkListener,
        test: Boolean = false
    ): Boolean {
        var atLeastOneNewMediaFileDetected = false
        val tempMediaDir = File(tempMediaPath).also { it.mkdir() }

        formToDownload.manifest!!.mediaFiles.forEachIndexed { i, mediaFile ->
            stateListener.progressUpdate(i + 1)

            val tempMediaFile = File(tempMediaDir, mediaFile.filename)

            val existingFile = searchForExistingMediaFile(formsRepository, formToDownload, mediaFile)
            existingFile.let {
                if (it != null) {
                    if (Md5.getMd5Hash(it).contentEquals(mediaFile.hash)) {
                        FileUtils.copyFile(it, tempMediaFile)
                    } else {
                        val existingFileHash = Md5.getMd5Hash(it)
                        val file = formSource.fetchMediaFile(mediaFile.downloadUrl)
                        FileUtils.interuptablyWriteFile(file, tempMediaFile, tempDir, stateListener)

                        if (!Md5.getMd5Hash(tempMediaFile).contentEquals(existingFileHash)) {
                            if (test) {
                                throw Exception("Content does not equal")
                            }
                            atLeastOneNewMediaFileDetected = true
                        }
                    }
                } else {
                    if (test) {
                        throw Exception("File does not exist")
                    }
                    val file = formSource.fetchMediaFile(mediaFile.downloadUrl)
                    FileUtils.interuptablyWriteFile(file, tempMediaFile, tempDir, stateListener)
                    atLeastOneNewMediaFileDetected = true
                }
            }
        }

        return atLeastOneNewMediaFileDetected
    }

    private fun searchForExistingMediaFile(
        formsRepository: FormsRepository,
        formToDownload: ServerFormDetails,
        mediaFile: MediaFile
    ): File? {
        val allFormVersions = formsRepository.getAllByFormId(formToDownload.formId)
        return allFormVersions.sortedByDescending {
            it.date
        }.map { form: Form ->
            File(form.formMediaPath, mediaFile.filename)
        }.firstOrNull { file: File ->
            file.exists()
        }
    }
}
