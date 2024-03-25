package org.odk.collect.android.instancemanagement

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.application.Collect
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.android.entities.EntitiesRepositoryProvider
import org.odk.collect.android.formentry.FormEntryUseCases
import org.odk.collect.android.formmanagement.CollectFormEntryControllerFactory
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.ExternalizableFormDefCache
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.utilities.SavepointsRepositoryProvider
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.forms.instances.Instance
import java.io.File

class InstancesDataService(
    private val appState: AppState,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val savepointsRepositoryProvider: SavepointsRepositoryProvider,
    private val entitiesRepositoryProvider: EntitiesRepositoryProvider,
    private val storagePathProvider: StoragePathProvider,
    private val instanceSubmitScheduler: InstanceSubmitScheduler,
    private val projectsDataService: ProjectsDataService,
    private val changeLockProvider: ChangeLockProvider,
    private val onUpdate: () -> Unit
) {
    val editableCount: LiveData<Int> = appState.getLive(EDITABLE_COUNT_KEY, 0)
    val sendableCount: LiveData<Int> = appState.getLive(SENDABLE_COUNT_KEY, 0)
    val sentCount: LiveData<Int> = appState.getLive(SENT_COUNT_KEY, 0)
    val instances: Flow<List<Instance>> = appState.getFlow("instances", emptyList())

    fun update() {
        val instancesRepository = instancesRepositoryProvider.get()

        val sendableInstances = instancesRepository.getCountByStatus(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        )
        val sentInstances = instancesRepository.getCountByStatus(
            Instance.STATUS_SUBMITTED,
            Instance.STATUS_SUBMISSION_FAILED
        )
        val editableInstances = instancesRepository.getCountByStatus(
            Instance.STATUS_INCOMPLETE,
            Instance.STATUS_INVALID,
            Instance.STATUS_VALID
        )

        appState.setLive(EDITABLE_COUNT_KEY, editableInstances)
        appState.setLive(SENDABLE_COUNT_KEY, sendableInstances)
        appState.setLive(SENT_COUNT_KEY, sentInstances)
        appState.setFlow("instances", instancesRepository.all)

        onUpdate()
    }

    fun finalizeAllDrafts(): FinalizeAllResult {
        val instancesRepository = instancesRepositoryProvider.get()
        val formsRepository = formsRepositoryProvider.get()
        val savepointsRepository = savepointsRepositoryProvider.get()
        val entitiesRepository = entitiesRepositoryProvider.get()
        val projectRootDir = File(storagePathProvider.getProjectRootDirPath())

        val instances = instancesRepository.getAllByStatus(
            Instance.STATUS_INCOMPLETE,
            Instance.STATUS_INVALID,
            Instance.STATUS_VALID
        )

        val result = instances.fold(FinalizeAllResult(0, 0, false)) { result, instance ->
            val formDefAndForm = FormEntryUseCases.loadFormDef(
                instance,
                formsRepository,
                projectRootDir,
                ExternalizableFormDefCache()
            )

            if (formDefAndForm == null) {
                result.copy(failureCount = result.failureCount + 1)
            } else {
                val (formDef, form) = formDefAndForm

                val formMediaDir = File(form.formMediaPath)
                val formEntryController =
                    CollectFormEntryControllerFactory().create(formDef, formMediaDir)
                val formController = FormEntryUseCases.loadDraft(form, instance, formEntryController)
                if (formController == null) {
                    result.copy(failureCount = result.failureCount + 1)
                } else {
                    val savePoint = savepointsRepository.get(form.dbId, instance.dbId)
                    val needsEncrypted = form.basE64RSAPublicKey != null
                    val newResult = if (savePoint != null) {
                        Analytics.log(AnalyticsEvents.BULK_FINALIZE_SAVE_POINT)
                        result.copy(failureCount = result.failureCount + 1, unsupportedInstances = true)
                    } else if (needsEncrypted) {
                        Analytics.log(AnalyticsEvents.BULK_FINALIZE_ENCRYPTED_FORM)
                        result.copy(failureCount = result.failureCount + 1, unsupportedInstances = true)
                    } else {
                        val finalizedInstance = FormEntryUseCases.finalizeDraft(
                            formController,
                            instancesRepository,
                            entitiesRepository
                        )

                        if (finalizedInstance == null) {
                            result.copy(failureCount = result.failureCount + 1)
                        } else {
                            result
                        }
                    }

                    Collect.getInstance().externalDataManager?.close()
                    newResult
                }
            }
        }

        update()
        instanceSubmitScheduler.scheduleSubmit(projectsDataService.getCurrentProject().uuid)

        return result.copy(successCount = instances.size - result.failureCount)
    }

    fun deleteInstances(instanceIds: LongArray): Boolean {
        return changeLockProvider.getInstanceLock(projectsDataService.getCurrentProject().uuid).withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                instanceIds.forEach { instanceId ->
                    InstanceDeleter(instancesRepositoryProvider.get(), formsRepositoryProvider.get()).delete(
                        instanceId
                    )

                    update()
                }

                update()
                true
            } else {
                false
            }
        }
    }

    companion object {
        private const val EDITABLE_COUNT_KEY = "instancesEditableCount"
        private const val SENDABLE_COUNT_KEY = "instancesSendableCount"
        private const val SENT_COUNT_KEY = "instancesSentCount"
    }
}

data class FinalizeAllResult(val successCount: Int, val failureCount: Int, val unsupportedInstances: Boolean)
