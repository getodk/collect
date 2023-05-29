package org.odk.collect.android.formmanagement

import org.odk.collect.android.utilities.FileUtils.copyFile
import org.odk.collect.android.utilities.FileUtils.interuptablyWriteFile
import org.odk.collect.async.OngoingWorkListener
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.MediaFile
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.File
import java.io.IOException

class FormMediaDownloader(
    private val formsRepository: FormsRepository,
    private val formSource: FormSource
) {

    @JvmOverloads
    @Throws(IOException::class, FormSourceException::class, InterruptedException::class)
    fun download(
        formToDownload: ServerFormDetails,
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

            val existingFile = searchForExistingMediaFile(formToDownload, mediaFile)
            existingFile.let {
                if (it != null) {
                    val fileHash = getMd5Hash(it)
                    if (fileHash.contentEquals(mediaFile.hash)) {
                        copyFile(it, tempMediaFile)
                    } else {
                        if (test) {
                            throw Exception("Content does not equal: $fileHash but mediafilehash: ${mediaFile.hash}")
                        }
                        val existingFileHash = getMd5Hash(it)
                        val file = formSource.fetchMediaFile(mediaFile.downloadUrl)
                        interuptablyWriteFile(file, tempMediaFile, tempDir, stateListener)

                        if (!getMd5Hash(tempMediaFile).contentEquals(existingFileHash)) {
                            atLeastOneNewMediaFileDetected = true
                        }
                    }
                } else {
                    if (test) {
                        throw Exception("File does not exist")
                    }
                    val file = formSource.fetchMediaFile(mediaFile.downloadUrl)
                    interuptablyWriteFile(file, tempMediaFile, tempDir, stateListener)
                    atLeastOneNewMediaFileDetected = true
                }
            }
        }

        return atLeastOneNewMediaFileDetected
    }

    private fun searchForExistingMediaFile(
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
