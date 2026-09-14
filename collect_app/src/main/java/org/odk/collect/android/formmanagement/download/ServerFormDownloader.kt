package org.odk.collect.android.formmanagement.download

import org.odk.collect.android.formmanagement.FormResult
import org.odk.collect.android.formmanagement.MediaFilesDownload
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.android.formmanagement.ServerFormUseCases
import org.odk.collect.android.formmanagement.ServerFormUseCases.ingestEntityListsFromDownload
import org.odk.collect.android.formmanagement.download.FormDownloadException.DiskError
import org.odk.collect.android.formmanagement.download.FormDownloadException.DownloadingInterrupted
import org.odk.collect.android.formmanagement.download.FormDownloadException.FormParsingError
import org.odk.collect.android.formmanagement.download.FormDownloadException.FormSourceError
import org.odk.collect.android.formmanagement.download.FormDownloadException.FormWithNoHash
import org.odk.collect.android.formmanagement.download.FormDownloadException.InvalidSubmission
import org.odk.collect.android.formmanagement.download.FormDownloader.ProgressReporter
import org.odk.collect.android.formmanagement.metadata.FormMetadata
import org.odk.collect.android.formmanagement.metadata.FormMetadataParser
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.FormNameUtils
import org.odk.collect.androidshared.utils.Validator.isUrlValid
import org.odk.collect.async.OngoingWorkListener
import org.odk.collect.entities.server.EntitySource
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormsRepository
import org.odk.collect.shared.files.FileExt.deleteDirectory
import org.odk.collect.shared.strings.Md5.getMd5Hash
import timber.log.Timber.Forest.d
import timber.log.Timber.Forest.e
import timber.log.Timber.Forest.i
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.function.Supplier

class ServerFormDownloader(
    private val formSource: FormSource,
    private val formsRepository: FormsRepository,
    private val cacheDir: File,
    private val formsDirPath: String,
    private val formMetadataParser: FormMetadataParser,
    private val clock: Supplier<Long>,
    private val entitiesRepository: EntitiesRepository,
    private val entitySource: EntitySource
) : FormDownloader {

    @Throws(FormDownloadException::class)
    override fun downloadForm(
        form: ServerFormDetails,
        progressReporter: ProgressReporter?,
        isCancelled: Supplier<Boolean>?
    ) {
        var preExistingFormsWithSameIdAndVersion: MutableList<Form> = ArrayList()

        val formOnDevice = if (!form.hash.isNullOrEmpty()) {
            formsRepository.getOneByMd5Hash(form.hash)
        } else {
            throw FormWithNoHash()
        }

        if (formOnDevice != null) {
            if (formOnDevice.isDeleted) {
                formsRepository.restore(formOnDevice.dbId)
            }
        } else {
            preExistingFormsWithSameIdAndVersion =
                formsRepository.getAllByFormIdAndVersion(form.formId, form.formVersion)
        }

        val tempDir = File(cacheDir, "download-" + UUID.randomUUID().toString())
        tempDir.mkdirs()

        try {
            val (formFileDownload, mediaFilesDownload) = try {
                val stateListener: OngoingWorkListener =
                    ProgressReporterAndSupplierStateListener(progressReporter, isCancelled)
                processOneForm(form, stateListener, tempDir, formsDirPath)
            } catch (e: FormSourceException) {
                throw FormSourceError(e)
            }

            try {
                installEverything(formFileDownload, mediaFilesDownload, formsDirPath)
            } catch (e: FormSourceException) {
                cleanUp(formFileDownload, mediaFilesDownload.tempMediaPath)
                throw FormSourceError(e)
            } catch (e: Exception) {
                cleanUp(formFileDownload, mediaFilesDownload.tempMediaPath)
                throw e
            }
        } finally {
            tempDir.deleteDirectory()
            for (formToDelete in preExistingFormsWithSameIdAndVersion) {
                formsRepository.delete(formToDelete.dbId)
            }
        }
    }

    @Throws(FormDownloadException::class, FormSourceException::class)
    private fun processOneForm(
        fd: ServerFormDetails,
        stateListener: OngoingWorkListener,
        tempDir: File,
        formsDirPath: String
    ): Pair<FormFileDownload, MediaFilesDownload> {
        val tempMediaPath = File(tempDir, "media").absolutePath

        // get the xml file
        // if we've downloaded a duplicate, this gives us the original file
        val formFileDownload = try {
            downloadXform(fd.formName, fd.downloadUrl, stateListener, tempDir, formsDirPath)
        } catch (_: InterruptedException) {
            cleanUp(null, tempMediaPath)
            throw DownloadingInterrupted()
        }

        return try {
            // download media files if there are any
            val mediaFilesDownload = if (fd.manifest != null && !fd.manifest.mediaFiles.isEmpty()) {
                ServerFormUseCases.downloadMediaFiles(
                    fd,
                    formSource,
                    formsRepository,
                    tempMediaPath,
                    tempDir,
                    entitiesRepository,
                    stateListener
                )
            } else {
                MediaFilesDownload(tempMediaPath, false, mutableListOf())
            }

            ServerFormUseCases.copySavedFileFromPreviousFormVersionIfExists(
                formsRepository,
                fd.formId!!,
                tempMediaPath
            )

            if (stateListener.isCancelled) {
                cleanUp(formFileDownload, tempMediaPath)
                throw DownloadingInterrupted()
            }

            Pair(formFileDownload, mediaFilesDownload)
        } catch (e: DownloadingInterrupted) {
            i(e)
            cleanUp(formFileDownload, tempMediaPath)
            throw DownloadingInterrupted()
        } catch (e: InterruptedException) {
            i(e)
            cleanUp(formFileDownload, tempMediaPath)
            throw DownloadingInterrupted()
        } catch (_: IOException) {
            throw DiskError()
        }
    }

    @Throws(
        DiskError::class,
        FormParsingError::class,
        InvalidSubmission::class,
        FormSourceException::class
    )
    private fun installEverything(
        formFileDownload: FormFileDownload,
        mediaFilesDownload: MediaFilesDownload,
        formsDirPath: String
    ) {
        val formMetadata = try {
            val start = System.currentTimeMillis()
            i("Parsing document %s", formFileDownload.file.absolutePath)

            formMetadataParser.readMetadata(formFileDownload.file).also {
                i("Parse finished in %.3f seconds.", (System.currentTimeMillis() - start) / 1000f)
            }
        } catch (e: RuntimeException) {
            throw FormParsingError(e)
        }

        if (formFileDownload.isNew && !isSubmissionOk(formMetadata)) {
            throw InvalidSubmission()
        }

        val formFile = if (formFileDownload.isNew) {
            File(formsDirPath, formFileDownload.file.name).also {
                FileUtils.copyFile(formFileDownload.file, it)
            }
        } else {
            formFileDownload.file
        }

        if (!formFileDownload.isNew) {
            if (mediaFilesDownload.newAttachmentsDownloaded) {
                val existingForm = formsRepository.getOneByPath(formFile.absolutePath)
                if (existingForm != null) {
                    formsRepository.save(
                        Form.Builder(existingForm)
                            .lastDetectedAttachmentsUpdateDate(clock.get())
                            .build()
                    )
                }
            }

            if (mediaFilesDownload.entitiesDownloaded) {
                val existingForm = formsRepository.getOneByPath(formFile.absolutePath)
                if (existingForm != null) {
                    formsRepository.save(
                        Form.Builder(existingForm)
                            .usesEntities(true)
                            .build()
                    )
                }
            }
        }

        // Save form in database
        val formResult = findOrCreateForm(formFile, formMetadata, mediaFilesDownload)

        ingestEntityListsFromDownload(
            formResult,
            mediaFilesDownload,
            entitiesRepository,
            entitySource,
            formsRepository
        )

        // move the media files in the media folder
        val tempMediaPath = mediaFilesDownload.tempMediaPath
        val formMediaDir = File(formResult.form.formMediaPath)
        try {
            moveMediaFiles(tempMediaPath, formMediaDir)
        } catch (e: IOException) {
            e(e)
            throw DiskError()
        }
    }

    private fun isSubmissionOk(formMetadata: FormMetadata): Boolean {
        val submission = formMetadata.submissionUri
        return submission == null || isUrlValid(submission)
    }

    private fun cleanUp(formFileDownload: FormFileDownload?, tempMediaPath: String) {
        if (formFileDownload == null) {
            d("The user cancelled (or an exception happened) the download of a form at the very beginning.")
        } else {
            if (formFileDownload.isNew) {
                val md5Hash = formFileDownload.file.getMd5Hash()
                if (md5Hash != null) {
                    val form = formsRepository.getOneByMd5Hash(md5Hash)
                    if (form != null) {
                        formsRepository.delete(form.dbId)
                    }
                }
            }
        }

        FileUtils.purgeMediaPath(tempMediaPath)
    }

    private fun findOrCreateForm(
        formFile: File,
        formMetadata: FormMetadata,
        mediaFilesDownload: MediaFilesDownload
    ): FormResult {
        val formFilePath = formFile.absolutePath
        val mediaPath = FileUtils.constructMediaPath(formFilePath)

        val existingForm = formsRepository.getOneByPath(formFile.absolutePath)

        if (existingForm == null) {
            val newForm = saveNewForm(
                formMetadata,
                formFile,
                mediaPath,
                mediaFilesDownload.entitiesDownloaded
            )
            return FormResult(newForm, true)
        } else {
            return FormResult(existingForm, false)
        }
    }

    private fun saveNewForm(
        formMetadata: FormMetadata,
        formFile: File,
        mediaPath: String?,
        entityAttachmentsDetected: Boolean
    ): Form {
        val form = Form.Builder()
            .formFilePath(formFile.absolutePath)
            .formMediaPath(mediaPath)
            .displayName(formMetadata.title)
            .version(formMetadata.version)
            .formId(formMetadata.id)
            .submissionUri(formMetadata.submissionUri)
            .base64RSAPublicKey(formMetadata.base64RsaPublicKey)
            .autoDelete(formMetadata.autoDelete)
            .autoSend(formMetadata.autoSend)
            .geometryXpath(formMetadata.geometryXPath)
            .usesEntities(formMetadata.isEntityForm || entityAttachmentsDetected)
            .build()

        return formsRepository.save(form)
    }

    /**
     * Takes the formName and the URL and attempts to download the specified file. Returns a file
     * object representing the downloaded file.
     */
    @Throws(
        FormSourceException::class,
        IOException::class,
        DownloadingInterrupted::class,
        InterruptedException::class
    )
    private fun downloadXform(
        formName: String,
        url: String,
        stateListener: OngoingWorkListener,
        tempDir: File,
        formsDirPath: String
    ): FormFileDownload {
        val xform = formSource.fetchForm(url)

        val fileName: String = getFormFileName(formName, formsDirPath)
        val tempFormFile = File(tempDir.toString() + File.separator + fileName)
        FileUtils.interuptablyWriteFile(xform, tempFormFile, tempDir, stateListener)

        // we've downloaded the file, and we may have renamed it
        // make sure it's not the same as a file we already have
        val form = formsRepository.getOneByMd5Hash(tempFormFile.getMd5Hash()!!)
        if (form != null) {
            // delete the file we just downloaded, because it's a duplicate
            FileUtils.deleteAndReport(tempFormFile)

            // set the file returned to the file we already had
            return FormFileDownload(File(form.formFilePath), false)
        } else {
            return FormFileDownload(tempFormFile, true)
        }
    }

    private class FormFileDownload(val file: File, val isNew: Boolean)

    private class ProgressReporterAndSupplierStateListener(
        private val progressReporter: ProgressReporter?,
        private val isCancelledProvider: Supplier<Boolean>?
    ) : OngoingWorkListener {
        override fun progressUpdate(progress: Int) {
            progressReporter?.onDownloadingMediaFile(progress)
        }

        override val isCancelled: Boolean
            get() = isCancelledProvider?.get() ?: false
    }

    companion object {
        private fun getFormFileName(formName: String?, formsDirPath: String?): String {
            val formattedFormName = FormNameUtils.formatFilenameFromFormName(formName)
            var fileName = "$formattedFormName.xml"
            var i = 2
            while (File(formsDirPath + File.separator + fileName).exists()) {
                fileName = formattedFormName + "_" + i + ".xml"
                i++
            }
            return fileName
        }

        @Throws(IOException::class)
        private fun moveMediaFiles(tempMediaPath: String, formMediaPath: File) {
            val tempMediaFolder = File(tempMediaPath)
            val mediaFiles = tempMediaFolder.listFiles()

            if (mediaFiles != null && mediaFiles.size != 0) {
                for (mediaFile in mediaFiles) {
                    try {
                        org.apache.commons.io.FileUtils.copyFileToDirectory(
                            mediaFile,
                            formMediaPath
                        )
                    } catch (e: IllegalArgumentException) {
                        // This can happen if copyFileToDirectory is pointed at a file instead of a dir
                        throw IOException(e)
                    }
                }
            }
        }
    }
}
