package org.odk.collect.android.formmanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import org.odk.collect.android.formmanagement.download.FormDownloadException
import org.odk.collect.android.formmanagement.download.ServerFormDownloader
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer
import org.odk.collect.android.formmanagement.metadata.FormMetadataParser
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.projects.ProjectDependencyModule
import org.odk.collect.android.state.DataKeys
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.androidshared.data.DataService
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSourceException
import org.odk.collect.projects.ProjectDependencyFactory
import org.odk.collect.settings.keys.ProjectKeys
import java.io.File
import java.util.function.Supplier
import java.util.stream.Collectors

class FormsDataService(
    appState: AppState,
    private val notifier: Notifier,
    private val projectDependencyModuleFactory: ProjectDependencyFactory<ProjectDependencyModule>,
    private val clock: Supplier<Long>
) : DataService(appState) {

    private val forms by qualifiedData(DataKeys.FORMS, emptyList<Form>()) { projectId ->
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        projectDependencies.formsRepository.all
    }

    private val lastMatchFormsWithServerCompletionTime by qualifiedData(DataKeys.LAST_MATCH_FORMS_WITH_SERVER_COMPLETION_TIME, null) { projectId ->
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        projectDependencies.generalSettings.getLong(DataKeys.LAST_MATCH_FORMS_WITH_SERVER_COMPLETION_TIME)
    }

    private val lastMatchFormsWithServerCompleted by qualifiedData(DataKeys.LAST_MATCH_FORMS_WITH_SERVER_COMPLETED, true) { projectId ->
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        projectDependencies.generalSettings.getBoolean(DataKeys.LAST_MATCH_FORMS_WITH_SERVER_COMPLETED)
    }

    private val syncing by qualifiedData(DataKeys.SYNC_STATUS_SYNCING, false)
    private val serverError by qualifiedData<FormSourceException?>(DataKeys.SYNC_STATUS_ERROR, null)
    private val diskError by qualifiedData<String?>(DataKeys.DISK_ERROR, null)

    fun getForms(projectId: String): Flow<List<Form>> {
        return forms.flow(projectId)
    }

    fun getLastMatchFormsWithServerCompletionTime(projectId: String): LiveData<Long?> {
        return lastMatchFormsWithServerCompletionTime.flow(projectId).asLiveData()
    }

    fun getLastMatchFormsWithServerCompleted(projectId: String): LiveData<Boolean> {
        return lastMatchFormsWithServerCompleted.flow(projectId).asLiveData()
    }

    fun isSyncing(projectId: String): LiveData<Boolean> {
        return syncing.flow(projectId).asLiveData()
    }

    fun getServerError(projectId: String): LiveData<FormSourceException?> {
        return serverError.flow(projectId).asLiveData()
    }

    fun getDiskError(projectId: String): LiveData<String?> {
        return diskError.flow(projectId).asLiveData()
    }

    fun clear(projectId: String) {
        serverError.set(projectId, null)
    }

    fun refresh(projectId: String) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        projectDependencies.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                startSync(projectId)
                syncWithStorage(projectId)
                update(projectId)
                finishSyncWithStorage(projectId)
            }
        }
    }

    fun downloadForms(
        projectId: String,
        forms: List<ServerFormDetails>,
        progressReporter: (Int, Int) -> Unit,
        isCancelled: () -> Boolean
    ): Map<ServerFormDetails, FormDownloadException?> {
        val results = mutableMapOf<ServerFormDetails, FormDownloadException?>()

        val projectDependencyModule = projectDependencyModuleFactory.create(projectId)
        projectDependencyModule.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                val formDownloader =
                    formDownloader(projectDependencyModule, clock)

                results.putAll(ServerFormUseCases.downloadForms(
                    forms,
                    formDownloader,
                    progressReporter,
                    isCancelled
                ))
            }
        }
        return results
    }

    /**
     * Downloads updates for the project's already downloaded forms. If Automatic download is
     * disabled the user will just be notified that there are updates available.
     */
    fun downloadUpdates(projectId: String) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        projectDependencies.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                syncWithStorage(projectId)

                val serverFormsDetailsFetcher = serverFormsDetailsFetcher(projectDependencies)
                val formDownloader = formDownloader(projectDependencies, clock)

                try {
                    val serverForms: List<ServerFormDetails> =
                        serverFormsDetailsFetcher.fetchFormDetails()
                    val updatedForms =
                        serverForms.stream().filter { obj: ServerFormDetails -> obj.isUpdated }
                            .collect(Collectors.toList())
                    if (updatedForms.isNotEmpty()) {
                        if (projectDependencies.generalSettings.getBoolean(ProjectKeys.KEY_AUTOMATIC_UPDATE)) {
                            val results = ServerFormUseCases.downloadForms(
                                updatedForms,
                                formDownloader
                            )

                            notifier.onUpdatesDownloaded(results, projectId)
                        } else {
                            notifier.onUpdatesAvailable(updatedForms, projectId)
                        }
                    }

                    update(projectId)
                } catch (_: FormSourceException) {
                    // Ignored
                }
            }
        }
    }

    /**
     * Downloads new forms, updates existing forms and deletes forms that are no longer part of
     * the project's form list.
     */
    @JvmOverloads
    fun matchFormsWithServer(projectId: String, notify: Boolean = true): Boolean {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        return projectDependencies.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                startSync(projectId)
                syncWithStorage(projectId)

                val serverFormsDetailsFetcher = serverFormsDetailsFetcher(projectDependencies)
                val formDownloader = formDownloader(projectDependencies, clock)

                val serverFormsSynchronizer = ServerFormsSynchronizer(
                    serverFormsDetailsFetcher,
                    projectDependencies.formsRepository,
                    projectDependencies.instancesRepository,
                    formDownloader
                )

                val exception = try {
                    serverFormsSynchronizer.synchronize()
                    if (notify) {
                        notifier.onSync(null, projectId)
                    }
                    lastMatchFormsWithServerCompleted(projectId)
                    null
                } catch (e: FormSourceException) {
                    if (notify) {
                        notifier.onSync(e, projectId)
                    }
                    lastMatchFormsWithServerFailed(projectId)
                    e
                }

                update(projectId)
                finishSyncWithServer(projectId, exception)
                exception == null
            } else {
                false
            }
        }
    }

    fun deleteForm(projectId: String, formId: Long) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        LocalFormUseCases.deleteForm(
            projectDependencies.formsRepository,
            projectDependencies.instancesRepository,
            formId
        )

        update(projectId)
    }

    fun lastMatchFormsWithServerFailed(projectId: String) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        projectDependencies.generalSettings.save(DataKeys.LAST_MATCH_FORMS_WITH_SERVER_COMPLETED, false)
        lastMatchFormsWithServerCompleted.set(projectId, false)
    }

    private fun lastMatchFormsWithServerCompleted(projectId: String) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        val currentTime = clock.get()
        projectDependencies.generalSettings.save(DataKeys.LAST_MATCH_FORMS_WITH_SERVER_COMPLETION_TIME, currentTime)
        lastMatchFormsWithServerCompletionTime.set(projectId, currentTime)
        projectDependencies.generalSettings.save(DataKeys.LAST_MATCH_FORMS_WITH_SERVER_COMPLETED, true)
        lastMatchFormsWithServerCompleted.set(projectId, true)
    }

    private fun syncWithStorage(projectId: String) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        val error = LocalFormUseCases.synchronizeWithDisk(
            projectDependencies.formsRepository,
            projectDependencies.formsDir
        )

        diskError.set(projectId, error)
    }

    private fun startSync(projectId: String) {
        syncing.set(projectId, true)
    }

    private fun finishSyncWithServer(projectId: String, exception: FormSourceException? = null) {
        serverError.set(projectId, exception)
        syncing.set(projectId, false)
    }

    private fun finishSyncWithStorage(projectId: String) {
        syncing.set(projectId, false)
    }
}

private fun formDownloader(
    projectDependencyModule: ProjectDependencyModule,
    clock: Supplier<Long>
): ServerFormDownloader {
    return ServerFormDownloader(
        projectDependencyModule.formSource,
        projectDependencyModule.formsRepository,
        File(projectDependencyModule.cacheDir),
        projectDependencyModule.formsDir,
        FormMetadataParser,
        clock,
        projectDependencyModule.entitiesRepository,
        projectDependencyModule.entitySource
    )
}

private fun serverFormsDetailsFetcher(
    projectDependencyModule: ProjectDependencyModule
): ServerFormsDetailsFetcher {
    return ServerFormsDetailsFetcher(
        projectDependencyModule.formsRepository,
        projectDependencyModule.formSource
    )
}
