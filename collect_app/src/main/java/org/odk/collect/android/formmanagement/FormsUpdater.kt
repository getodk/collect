package org.odk.collect.android.formmanagement

import android.content.Context
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.analytics.AnalyticsUtils
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.forms.FormSourceException
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.strings.localization.getLocalizedString
import java.io.File
import java.util.stream.Collectors

class FormsUpdater(
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
        val sandbox = getProjectSandbox(projectId)

        val diskFormsSynchronizer = diskFormsSynchronizer(sandbox)
        val serverFormsDetailsFetcher = serverFormsDetailsFetcher(sandbox, diskFormsSynchronizer)
        val formDownloader = formDownloader(sandbox, analytics)

        try {
            val serverForms: List<ServerFormDetails> = serverFormsDetailsFetcher.fetchFormDetails()
            val updatedForms =
                serverForms.stream().filter { obj: ServerFormDetails -> obj.isUpdated }
                    .collect(Collectors.toList())
            if (updatedForms.isNotEmpty()) {
                if (sandbox.generalSettings.getBoolean(ProjectKeys.KEY_AUTOMATIC_UPDATE)) {
                    val formUpdateDownloader = FormUpdateDownloader()
                    val results = formUpdateDownloader.downloadUpdates(
                        updatedForms,
                        sandbox.formsLock,
                        formDownloader,
                        context.getLocalizedString(R.string.success),
                        context.getLocalizedString(R.string.failure)
                    )

                    notifier.onUpdatesDownloaded(results, projectId)
                } else {
                    notifier.onUpdatesAvailable(updatedForms, projectId)
                }
            }

            context.contentResolver.notifyChange(FormsContract.getUri(projectId), null)
        } catch (_: FormSourceException) {
            // Ignored
        }
    }

    /**
     * Downloads new forms, updates existing forms and deletes forms that are no longer part of
     * the project's form list.
     */
    @JvmOverloads
    fun matchFormsWithServer(projectId: String, notify: Boolean = true): Boolean {
        val sandbox = getProjectSandbox(projectId)

        val diskFormsSynchronizer = diskFormsSynchronizer(sandbox)
        val serverFormsDetailsFetcher = serverFormsDetailsFetcher(sandbox, diskFormsSynchronizer)
        val formDownloader = formDownloader(sandbox, analytics)

        val serverFormsSynchronizer = ServerFormsSynchronizer(
            serverFormsDetailsFetcher,
            sandbox.formsRepository,
            sandbox.instancesRepository,
            formDownloader
        )

        return sandbox.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                syncStatusAppState.startSync(projectId)

                val exception = try {
                    serverFormsSynchronizer.synchronize()
                    syncStatusAppState.finishSync(projectId, null)
                    if (notify) {
                        notifier.onSync(null, projectId)
                        AnalyticsUtils.logMatchExactlyCompleted(analytics, null)
                    }
                    null
                } catch (e: FormSourceException) {
                    syncStatusAppState.finishSync(projectId, e)
                    if (notify) {
                        notifier.onSync(e, projectId)
                        AnalyticsUtils.logMatchExactlyCompleted(analytics, e)
                    }
                    e
                }

                exception == null
            } else {
                false
            }
        }
    }

    private fun getProjectSandbox(projectId: String) = ProjectSandbox(
        projectId,
        settingsProvider,
        formsRepositoryProvider,
        instancesRepositoryProvider,
        storagePathProvider,
        changeLockProvider,
        formSourceProvider
    )
}

/**
 * Provides all the basic/building block dependencies needed when performing logic inside a
 * project.
 */
private class ProjectSandbox(
    private val projectId: String,
    private val settingsProvider: SettingsProvider,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val storagePathProvider: StoragePathProvider,
    private val changeLockProvider: ChangeLockProvider,
    private val formSourceProvider: FormSourceProvider
) {

    val generalSettings by lazy { settingsProvider.getUnprotectedSettings(projectId) }
    val formsRepository by lazy { formsRepositoryProvider.get(projectId) }
    val instancesRepository by lazy { instancesRepositoryProvider.get(projectId) }
    val formSource by lazy { formSourceProvider.get(projectId) }
    val formsLock by lazy { changeLockProvider.getFormLock(projectId) }
    val formsDir by lazy { storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, projectId) }
    val cacheDir by lazy { storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, projectId) }
}

private fun formDownloader(
    projectSandbox: ProjectSandbox,
    analytics: Analytics
): ServerFormDownloader {
    return ServerFormDownloader(
        projectSandbox.formSource,
        projectSandbox.formsRepository,
        File(projectSandbox.cacheDir),
        projectSandbox.formsDir,
        FormMetadataParser(),
        analytics
    )
}

private fun serverFormsDetailsFetcher(
    projectSandbox: ProjectSandbox,
    diskFormsSynchronizer: FormsDirDiskFormsSynchronizer
): ServerFormsDetailsFetcher {
    return ServerFormsDetailsFetcher(
        projectSandbox.formsRepository,
        projectSandbox.formSource,
        diskFormsSynchronizer
    )
}

private fun diskFormsSynchronizer(projectSandbox: ProjectSandbox): FormsDirDiskFormsSynchronizer {
    return FormsDirDiskFormsSynchronizer(
        projectSandbox.formsRepository,
        projectSandbox.formsDir
    )
}
