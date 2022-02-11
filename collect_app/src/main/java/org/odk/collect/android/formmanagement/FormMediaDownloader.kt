package org.odk.collect.android.formmanagement

import org.odk.collect.android.utilities.FileUtils.constructMediaPath
import org.odk.collect.android.utilities.FileUtils.copyFile
import org.odk.collect.android.utilities.FileUtils.interuptablyWriteFile
import org.odk.collect.async.OngoingWorkListener
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.MediaFile
import org.odk.collect.shared.strings.Md5.getMd5Hash
import timber.log.Timber
import java.io.File
import java.io.IOException

class FormMediaDownloader constructor(
    private val formsDirPath: String,
    private val formsRepository: FormsRepository,
    private val formSource: FormSource
) {

    @Throws(IOException::class, FormSourceException::class, InterruptedException::class)
    fun download(
        tempMediaPath: String,
        stateListener: OngoingWorkListener?,
        files: List<MediaFile>,
        tempDir: File,
        formFileName: String,
        fd: ServerFormDetails
    ) {
        val tempMediaDir = File(tempMediaPath).also { it.mkdir() }

        for (i in files.indices) {
            stateListener?.progressUpdate(i + 1)

            val (filename, hash, downloadUrl) = files[i]
            val tempMediaFile = File(tempMediaDir, filename)
            val finalMediaPath = constructMediaPath(
                formsDirPath + File.separator + formFileName
            )
            val finalMediaFile = File(finalMediaPath, filename)

            if (!finalMediaFile.exists()) {
                val allFormVersions = formsRepository.getAllByFormId(fd.formId)
                val existingFileInOtherVersion = allFormVersions.map { form: Form ->
                    File(
                        form.formMediaPath,
                        filename
                    )
                }.firstOrNull { file: File ->
                    val currentFileHash = getMd5Hash(file)
                    val downloadFileHash = validateHash(hash)
                    file.exists() && currentFileHash.contentEquals(downloadFileHash)
                }

                if (existingFileInOtherVersion != null) {
                    copyFile(existingFileInOtherVersion, tempMediaFile)
                } else {
                    val mediaFile = formSource.fetchMediaFile(downloadUrl)
                    interuptablyWriteFile(mediaFile, tempMediaFile, tempDir, stateListener)
                }
            } else {
                val currentFileHash = getMd5Hash(finalMediaFile)
                val downloadFileHash = validateHash(hash)
                if (currentFileHash != null && downloadFileHash != null && !currentFileHash.contentEquals(
                        downloadFileHash
                    )
                ) {
                    // if the hashes match, it's the same file otherwise replace it with the new one
                    val mediaFile = formSource.fetchMediaFile(downloadUrl)
                    interuptablyWriteFile(
                        mediaFile,
                        tempMediaFile,
                        tempDir,
                        stateListener
                    )
                } else {
                    // exists, and the hash is the same
                    // no need to download it again
                    Timber.i(
                        "Skipping media file fetch -- file hashes identical: %s",
                        finalMediaFile.absolutePath
                    )
                }
            }
        }
    }

    private fun validateHash(hash: String?): String? {
        return if (hash == null || hash.isEmpty()) null else hash
    }
}
