package org.odk.collect.android.formmanagement

import android.content.Context
import org.javarosa.core.reference.ReferenceManager
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.analytics.AnalyticsUtils
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.provider.FormsProviderAPI
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.utilities.TranslationHandler
import org.odk.collect.forms.FormSourceException
import org.odk.collect.shared.Settings
import org.odk.collect.shared.locks.ChangeLock
import java.io.File
import java.util.stream.Collectors

class FormUpdateChecker(
    private val context: Context,
    private val notifier: Notifier,
    private val analytics: Analytics,
    private val storagePathProvider: StoragePathProvider,
    private val settingsProvider: SettingsProvider,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val formSourceProvider: FormSourceProvider,
    private val syncStatusAppState: SyncStatusAppState,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val changeLockProvider: ChangeLockProvider
) {

    /**
     * Downloads updates for the project's already downloaded forms. If Automatic download is
     * disabled the user will just be notified that there are updates available.
     */
    fun downloadUpdates(projectId: String) {
        val formsDirPath = formsDir(projectId)
        val formsRepository = formsRepository(projectId)
        val formSource = formSource(projectId)
        val diskFormsSynchronizer: DiskFormsSynchronizer = FormsDirDiskFormsSynchronizer(
            formsRepository,
            formsDirPath
        )

        val serverFormsDetailsFetcher = ServerFormsDetailsFetcher(
            formsRepository,
            formSource,
            diskFormsSynchronizer
        )

        val cacheDirPath: String = formsCacheDir(projectId)
        val formDownloader = ServerFormDownloader(
            formSource,
            formsRepository,
            File(cacheDirPath),
            formsDirPath,
            FormMetadataParser(ReferenceManager.instance()),
            analytics
        )

        val generalSettings = generalSettings(projectId)
        val changeLock = changeLockProvider.getFormLock(projectId)

        downloadUpdates(serverFormsDetailsFetcher, generalSettings, changeLock, formDownloader)
    }

    fun synchronizeWithServer(projectId: String): Boolean {
        val formsDirPath = formsDir(projectId)
        val cacheDirPath = formsCacheDir(projectId)
        val formsRepository = formsRepository(projectId)
        val formSource = formSource(projectId)

        val diskFormsSynchronizer = FormsDirDiskFormsSynchronizer(
            formsRepository,
            formsDirPath
        )

        val serverFormsDetailsFetcher = ServerFormsDetailsFetcher(
            formsRepository,
            formSource,
            diskFormsSynchronizer
        )

        val instancesRepository = instancesRepositoryProvider.get(projectId)
        val formDownloader = ServerFormDownloader(
            formSource,
            formsRepository,
            File(cacheDirPath),
            formsDirPath,
            FormMetadataParser(ReferenceManager.instance()),
            analytics
        )

        val serverFormsSynchronizer = ServerFormsSynchronizer(
            serverFormsDetailsFetcher,
            formsRepository,
            instancesRepository,
            formDownloader
        )

        val formLock = changeLockProvider.getFormLock(projectId)
        return synchronizeWithServer(formLock, serverFormsSynchronizer)
    }

    private fun synchronizeWithServer(
        formLock: ChangeLock,
        serverFormsSynchronizer: ServerFormsSynchronizer
    ): Boolean {
        return formLock.withLock { acquiredLock ->
            if (acquiredLock) {
                syncStatusAppState.startSync()

                val exception = try {
                    serverFormsSynchronizer.synchronize()
                    syncStatusAppState.finishSync(null)
                    notifier.onSync(null)
                    null
                } catch (e: FormSourceException) {
                    syncStatusAppState.finishSync(e)
                    notifier.onSync(e)
                    e
                }

                AnalyticsUtils.logMatchExactlyCompleted(analytics, exception)
                exception == null
            } else {
                false
            }
        }
    }

    private fun downloadUpdates(
        serverFormsDetailsFetcher: ServerFormsDetailsFetcher,
        generalSettings: Settings,
        changeLock: ChangeLock,
        formDownloader: ServerFormDownloader
    ) {
        try {
            val serverForms: List<ServerFormDetails> = serverFormsDetailsFetcher.fetchFormDetails()
            val updatedForms =
                serverForms.stream().filter { obj: ServerFormDetails -> obj.isUpdated }
                    .collect(Collectors.toList())
            if (updatedForms.isNotEmpty()) {
                if (generalSettings.getBoolean(GeneralKeys.KEY_AUTOMATIC_UPDATE)) {
                    val formUpdateDownloader = FormUpdateDownloader()
                    val results = formUpdateDownloader.downloadUpdates(
                        updatedForms,
                        changeLock,
                        formDownloader,
                        TranslationHandler.getString(context, R.string.success),
                        TranslationHandler.getString(context, R.string.failure)
                    )

                    notifier.onUpdatesDownloaded(results)
                } else {
                    notifier.onUpdatesAvailable(updatedForms)
                }
            }

            context.contentResolver.notifyChange(FormsProviderAPI.CONTENT_URI, null)
        } catch (_: FormSourceException) {
            // Ignored
        }
    }

    private fun generalSettings(projectId: String) =
        settingsProvider.getGeneralSettings(projectId)

    private fun formsDir(projectId: String) =
        storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, projectId)

    private fun formsCacheDir(projectId: String) =
        storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, projectId)

    private fun formSource(projectId: String) = formSourceProvider.get(projectId)

    private fun formsRepository(projectId: String) = formsRepositoryProvider.get(projectId)
}
