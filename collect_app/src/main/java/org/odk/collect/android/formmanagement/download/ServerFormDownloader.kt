package org.odk.collect.android.formmanagement.download

import org.odk.collect.android.formmanagement.FormFileDownload
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
import org.odk.collect.shared.result.Result
import org.odk.collect.shared.result.chain
import org.odk.collect.shared.result.map
import org.odk.collect.shared.result.mapError
import org.odk.collect.shared.result.onError
import org.odk.collect.shared.result.runAndCatch
import org.odk.collect.shared.result.toError
import org.odk.collect.shared.result.toSuccess
import org.odk.collect.shared.strings.Md5.getMd5Hash
import timber.log.Timber
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
        if (form.hash.isNullOrEmpty()) {
            throw FormWithNoHash()
        }

        val tempDir = File(cacheDir, "download-" + UUID.randomUUID().toString())
        tempDir.mkdirs()

        try {
            val (formFileDownload, mediaFilesDownload) = try {
                val stateListener = object : OngoingWorkListener {
                    override fun progressUpdate(progress: Int) {
                        progressReporter?.onDownloadingMediaFile(progress)
                    }

                    override val isCancelled: Boolean
                        get() = isCancelled?.get() ?: false
                }

                processOneForm(form, stateListener, tempDir, formsDirPath)
            } catch (e: FormSourceException) {
                throw FormSourceError(e)
            }

            installEverything(formFileDownload, mediaFilesDownload, formsDirPath)
                .onError { throw it }
        } finally {
            tempDir.deleteDirectory()
        }
    }

    @Throws(FormDownloadException::class, FormSourceException::class, DownloadingInterrupted::class)
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
            throw DownloadingInterrupted()
        } catch (_: IOException) {
            throw DiskError()
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
                MediaFilesDownload(tempMediaPath, false, emptyList())
            }

            if (stateListener.isCancelled) {
                throw DownloadingInterrupted()
            }

            Pair(formFileDownload, mediaFilesDownload)
        } catch (e: DownloadingInterrupted) {
            Timber.i(e)
            throw DownloadingInterrupted()
        } catch (e: InterruptedException) {
            Timber.i(e)
            throw DownloadingInterrupted()
        } catch (_: IOException) {
            throw DiskError()
        }
    }

    private fun installEverything(
        formFileDownload: FormFileDownload,
        mediaFilesDownload: MediaFilesDownload,
        formsDirPath: String
    ): Result<Unit, FormDownloadException> {
        ingestEntityListsFromDownload(
            mediaFilesDownload,
            entitiesRepository,
            entitySource,
        )

        return createOrUpdateForm(
            formFileDownload,
            mediaFilesDownload,
            formsDirPath
        ).chain { form ->
            moveMediaFiles(mediaFilesDownload.tempMediaPath, form)
                .map { form }
                .mapError { DiskError() }
        }.chain { form ->
            ServerFormUseCases.copySavedFileFromPreviousFormVersion(formsRepository, form)
            Unit.toSuccess()
        }.onError {
            // Clean up form if we created it
            if (formFileDownload is FormFileDownload.New) {
                val form = formsRepository.getOneByMd5Hash(formFileDownload.hash)
                if (form != null) {
                    formsRepository.delete(form.dbId)
                }
            }
        }
    }

    private fun createOrUpdateForm(
        formFileDownload: FormFileDownload,
        mediaFilesDownload: MediaFilesDownload,
        formsDirPath: String
    ): Result<Form, FormDownloadException> {
        val entityLists = mediaFilesDownload.entityLists

        return when (formFileDownload) {
            is FormFileDownload.Existing -> {
                ServerFormUseCases.updateForm(
                    formFileDownload,
                    mediaFilesDownload,
                    entitiesRepository,
                    formsRepository,
                    clock.get()
                ).toSuccess()
            }

            is FormFileDownload.New -> {
                parseFormMetadata(formFileDownload).chain {
                    if (!isSubmissionOk(it)) {
                        InvalidSubmission().toError()
                    } else {
                        val formFile = File(formsDirPath, formFileDownload.file.name)
                        FileUtils.copyFile(formFileDownload.file, formFile)

                        val newForm = saveNewForm(
                            it,
                            formFile,
                            entityLists.isNotEmpty()
                        )

                        newForm.toSuccess()
                    }
                }
            }
        }
    }

    private fun parseFormMetadata(formFileDownload: FormFileDownload.New): Result<FormMetadata, FormParsingError> {
        return runAndCatch {
            formMetadataParser.readMetadata(formFileDownload.file)
        }.mapError {
            FormParsingError(it)
        }
    }

    private fun saveNewForm(
        formMetadata: FormMetadata,
        formFile: File,
        entityAttachmentsDetected: Boolean
    ): Form {
        val formId = formMetadata.id
        val version = formMetadata.version

        // Account for server returning update with same id/version
        formsRepository.getAllByFormIdAndVersion(formId, version).forEach {
            formsRepository.delete(it.dbId)
        }

        val form = Form.Builder()
            .formFilePath(formFile.absolutePath)
            .formMediaPath(FileUtils.constructMediaPath(formFile.absolutePath))
            .displayName(formMetadata.title)
            .version(version)
            .formId(formId)
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
        val hash = tempFormFile.getMd5Hash()!!
        val form = formsRepository.getOneByMd5Hash(hash)
        if (form != null) {
            // delete the file we just downloaded, because it's a duplicate
            FileUtils.deleteAndReport(tempFormFile)
            return FormFileDownload.Existing(form)
        } else {
            return FormFileDownload.New(tempFormFile, hash)
        }
    }
}

private fun getFormFileName(formName: String, formsDirPath: String): String {
    val formattedFormName = FormNameUtils.formatFilenameFromFormName(formName)
    var fileName = "$formattedFormName.xml"
    val existingForms = (File(formsDirPath).listFiles() ?: emptyArray()).map { it.name }

    var i = 2
    while (existingForms.contains(fileName)) {
        fileName = formattedFormName + "_" + i + ".xml"
        i++
    }

    return fileName
}

private fun moveMediaFiles(tempMediaPath: String, form: Form): Result<File, IOException> {
    val tempMediaFolder = File(tempMediaPath)
    val formMediaDir = File(form.formMediaPath)
    tempMediaFolder.listFiles()?.forEach { mediaFile ->
        runAndCatch {
            org.apache.commons.io.FileUtils.copyFileToDirectory(mediaFile, formMediaDir)
        }.onError {
            return when (it) {
                is IOException -> it.toError()
                else -> IOException(it).toError()
            }
        }
    }

    return formMediaDir.toSuccess()
}

private fun isSubmissionOk(formMetadata: FormMetadata): Boolean {
    val submission = formMetadata.submissionUri
    return submission == null || isUrlValid(submission)
}
