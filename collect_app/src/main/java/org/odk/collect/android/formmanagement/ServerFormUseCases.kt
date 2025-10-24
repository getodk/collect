package org.odk.collect.android.formmanagement

import org.apache.commons.io.FileUtils.copyFileToDirectory
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.formmanagement.download.FormDownloadException
import org.odk.collect.android.formmanagement.download.FormDownloader
import org.odk.collect.android.instancemanagement.autosend.getLastUpdated
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.FormUtils
import org.odk.collect.async.OngoingWorkListener
import org.odk.collect.entities.LocalEntityUseCases
import org.odk.collect.entities.server.EntitySource
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.ManifestFile
import org.odk.collect.forms.MediaFile
import org.odk.collect.shared.strings.Md5.getMd5Hash
import timber.log.Timber
import java.io.File
import java.io.IOException

object ServerFormUseCases {

    @JvmStatic
    @Throws(FormSourceException::class)
    fun fetchFormDetails(
        formsRepository: FormsRepository,
        formSource: FormSource
    ): List<ServerFormDetails> {
        val formList = formSource.fetchFormList()
        return formList.map { listItem ->
            val manifestFile = listItem.manifestURL?.let {
                getManifestFile(formSource, it)
            }

            val forms = formsRepository.getAllNotDeletedByFormId(listItem.formID)
            val thisFormAlreadyDownloaded = forms.isNotEmpty()

            val formHash = listItem.hash
            val existingForm = if (formHash != null) {
                formsRepository.getOneByMd5Hash(formHash)
            } else {
                null
            }

            val isNewerFormVersionAvailable = listItem.hash.let {
                if (thisFormAlreadyDownloaded) {
                    existingForm == null
                } else {
                    false
                }
            }

            val areNewerMediaFilesAvailable = if (existingForm != null && manifestFile != null) {
                areNewerMediaFilesAvailable(existingForm, manifestFile.mediaFiles)
            } else {
                false
            }

            val type = if (existingForm != null) {
                if (existingForm.isDeleted) {
                    ServerFormDetails.Type.New
                } else if (areNewerMediaFilesAvailable) {
                    ServerFormDetails.Type.UpdatedMedia
                } else {
                    ServerFormDetails.Type.OnDevice
                }
            } else if (thisFormAlreadyDownloaded) {
                if (listItem.hash == null) {
                    ServerFormDetails.Type.OnDevice
                } else if (forms.any { it.version == listItem.version }) {
                    ServerFormDetails.Type.UpdatedHash
                } else {
                    ServerFormDetails.Type.UpdatedVersion
                }
            } else {
                ServerFormDetails.Type.New
            }

            ServerFormDetails(
                listItem.name,
                listItem.downloadURL,
                listItem.formID,
                listItem.version,
                listItem.hash,
                !thisFormAlreadyDownloaded,
                isNewerFormVersionAvailable || areNewerMediaFilesAvailable,
                manifestFile,
                type
            )
        }
    }

    fun downloadForms(
        forms: List<ServerFormDetails>,
        formDownloader: FormDownloader,
        progressReporter: ((Int, Int) -> Unit)? = null,
        isCancelled: (() -> Boolean)? = null,
    ): Map<ServerFormDetails, FormDownloadException?> {
        val results = mutableMapOf<ServerFormDetails, FormDownloadException?>()
        for (index in forms.indices) {
            val form = forms[index]

            try {
                formDownloader.downloadForm(
                    form,
                    object : FormDownloader.ProgressReporter {
                        override fun onDownloadingMediaFile(count: Int) {
                            progressReporter?.invoke(index, count)
                        }
                    },
                    { isCancelled?.invoke() ?: false }
                )

                results[form] = null
            } catch (e: FormDownloadException.DownloadingInterrupted) {
                break
            } catch (e: FormDownloadException) {
                results[form] = e
            }
        }

        return results
    }

    @JvmStatic
    fun copySavedFileFromPreviousFormVersionIfExists(
        formsRepository: FormsRepository,
        formId: String,
        mediaDirPath: String
    ) {
        val lastSavedFile: File? = formsRepository
            .getAllByFormId(formId)
            .maxByOrNull { form -> form.date }
            ?.let {
                File(it.formMediaPath, FileUtils.LAST_SAVED_FILENAME)
            }

        if (lastSavedFile != null && lastSavedFile.exists()) {
            File(mediaDirPath).mkdir()
            FileUtils.copyFile(lastSavedFile, File(mediaDirPath, FileUtils.LAST_SAVED_FILENAME))
        }
    }

    @JvmStatic
    @Throws(IOException::class, FormSourceException::class, InterruptedException::class)
    fun downloadMediaFiles(
        formToDownload: ServerFormDetails,
        formSource: FormSource,
        formsRepository: FormsRepository,
        tempMediaPath: String,
        tempDir: File,
        entitiesRepository: EntitiesRepository,
        entitySource: EntitySource,
        stateListener: OngoingWorkListener
    ): MediaFilesDownloadResult {
        var newAttachmentsDownloaded = false
        var entitiesDownloaded = false

        val tempMediaDir = File(tempMediaPath).also { it.mkdir() }

        val existingForm = formsRepository.getAllByFormIdAndVersion(formToDownload.formId, formToDownload.formVersion).firstOrNull()
        val allFormVersionsSorted = formsRepository.getAllByFormId(formToDownload.formId).sortedByDescending { it.date }
        val currentOrLastFormVersion = existingForm ?: allFormVersionsSorted.firstOrNull()

        formToDownload.manifest!!.mediaFiles.forEachIndexed { i, mediaFile ->
            stateListener.progressUpdate(i + 1)

            val tempMediaFile = File(tempMediaDir, mediaFile.filename)

            val isEntityList = mediaFile.type != null
            if (isEntityList) {
                val entityListName = getEntityListFromFileName(mediaFile)
                val localEntityList = entitiesRepository.getList(entityListName)

                entitiesDownloaded = true

                if (localEntityList == null || mediaFile.hash != localEntityList.hash) {
                    newAttachmentsDownloaded = true
                    downloadMediaFile(formSource, mediaFile, tempMediaFile, tempDir, stateListener)

                    LocalEntityUseCases.updateLocalEntitiesFromServer(
                        entityListName,
                        tempMediaFile,
                        entitiesRepository,
                        mediaFile
                    )
                } else {
                    val existingForm = formsRepository.getAllByFormIdAndVersion(
                        formToDownload.formId,
                        formToDownload.formVersion
                    ).getOrNull(0)

                    if (existingForm != null) {
                        val entityListLastUpdated = localEntityList.lastUpdated
                        if (entityListLastUpdated != null && entityListLastUpdated > existingForm.getLastUpdated()) {
                            newAttachmentsDownloaded = true
                        }
                    }
                }

                /**
                 * Ensures local offline Entities are cleaned up when they have been deleted on the server.
                 *
                 * Normally this cleanup is triggered during sync as part of a full update with the server
                 * whenever the Entity list hash from Central changes.
                 * However, there is a case where the hash stays the same:
                 *  - a sync happens and the current hash is stored,
                 *  - an Entity is created locally and a form is uploaded, creating the Entity on the server,
                 *  - the Entity is then deleted on the server,
                 *  - another sync occurs, but the hash is the same as the stored one because the list
                 *    contents are identical to before the local Entity was added.
                 *
                 * In this case, the usual hash-based update will not detect the deletion. Collect must
                 * use the integrityUrl to check for missing Entities and remove them locally.
                 */
                LocalEntityUseCases.cleanUpDeletedOfflineEntities(
                    entityListName,
                    entitiesRepository,
                    entitySource,
                    mediaFile
                )
            } else {
                val existingFile = searchForExistingMediaFile(currentOrLastFormVersion, mediaFile)
                if (existingFile != null) {
                    val existingFileHash = existingFile.getMd5Hash()

                    if (existingFileHash.contentEquals(mediaFile.hash)) {
                        copyFileToDirectory(existingFile, tempMediaDir)
                    } else {
                        downloadMediaFile(
                            formSource,
                            mediaFile,
                            tempMediaFile,
                            tempDir,
                            stateListener
                        )

                        if (!tempMediaFile.getMd5Hash().contentEquals(existingFileHash)) {
                            newAttachmentsDownloaded = true
                        }
                    }
                } else {
                    downloadMediaFile(
                        formSource,
                        mediaFile,
                        tempMediaFile,
                        tempDir,
                        stateListener
                    )
                    newAttachmentsDownloaded = true
                }

                logEntityListClashes(mediaFile, entitiesRepository)
            }
        }

        return MediaFilesDownloadResult(newAttachmentsDownloaded, entitiesDownloaded)
    }

    private fun downloadMediaFile(
        formSource: FormSource,
        mediaFile: MediaFile,
        tempMediaFile: File,
        tempDir: File,
        stateListener: OngoingWorkListener
    ) {
        val file = formSource.fetchMediaFile(mediaFile.downloadUrl)
        FileUtils.interuptablyWriteFile(file, tempMediaFile, tempDir, stateListener)
    }

    /**
     * Track CSVs that have names that clash with entity lists in the project. If
     * these CSVs are being used as part of a `select_one_from_file` question, the
     * instance ID will be the file name with the extension removed.
     */
    private fun logEntityListClashes(
        mediaFile: MediaFile,
        entitiesRepository: EntitiesRepository
    ) {
        val isCsv = mediaFile.filename.endsWith(".csv")
        val mostLikelyInstanceId = getEntityListFromFileName(mediaFile)
        if (isCsv && entitiesRepository.getList(mostLikelyInstanceId) != null) {
            Analytics.setUserProperty("HasEntityListCollision", "true")
        }
    }

    private fun getEntityListFromFileName(mediaFile: MediaFile) =
        mediaFile.filename.substringBefore(".csv")

    private fun searchForExistingMediaFile(
        currentOrLastFormVersion: Form?,
        mediaFile: MediaFile
    ): File? {
        return if (currentOrLastFormVersion != null) {
            val candidate = File(currentOrLastFormVersion.formMediaPath, mediaFile.filename)
            if (candidate.exists()) {
                candidate
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun getManifestFile(formSource: FormSource, manifestUrl: String): ManifestFile? {
        return try {
            formSource.fetchManifest(manifestUrl)
        } catch (formSourceException: FormSourceException) {
            Timber.w(formSourceException)
            null
        }
    }

    private fun areNewerMediaFilesAvailable(
        existingForm: Form,
        newMediaFiles: List<MediaFile>
    ): Boolean {
        if (newMediaFiles.isEmpty()) {
            return false
        }

        val localMediaHashes = FormUtils.getMediaFiles(existingForm)
            .map { it.getMd5Hash() }
            .toSet()

        return newMediaFiles.any {
            !it.filename.endsWith(".zip") && it.hash !in localMediaHashes
        }
    }
}

class EntityListUpdateException(cause: Throwable) : Exception(cause)

data class MediaFilesDownloadResult(
    val newAttachmentsDownloaded: Boolean,
    val entitiesDownloaded: Boolean
)
