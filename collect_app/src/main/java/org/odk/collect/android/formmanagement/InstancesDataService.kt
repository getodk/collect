package org.odk.collect.android.formmanagement

import androidx.lifecycle.LiveData
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.application.Collect
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.android.entities.EntitiesRepositoryProvider
import org.odk.collect.android.formentry.FormEntryUseCases
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.ExternalizableFormDefCache
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.forms.instances.Instance
import java.io.File

class InstancesDataService(
    private val appState: AppState,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val entitiesRepositoryProvider: EntitiesRepositoryProvider,
    private val storagePathProvider: StoragePathProvider,
    private val instanceSubmitScheduler: InstanceSubmitScheduler,
    private val currentProjectProvider: CurrentProjectProvider,
    private val onUpdate: () -> Unit
) {
    val editableCount: LiveData<Int> = appState.getLive(EDITABLE_COUNT_KEY, 0)
    val sendableCount: LiveData<Int> = appState.getLive(SENDABLE_COUNT_KEY, 0)
    val sentCount: LiveData<Int> = appState.getLive(SENT_COUNT_KEY, 0)

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

        onUpdate()
    }

    fun finalizeAllDrafts(): FinalizeAllResult {
        val instancesRepository = instancesRepositoryProvider.get()
        val formsRepository = formsRepositoryProvider.get()
        val entitiesRepository = entitiesRepositoryProvider.get()
        val projectRootDir = File(storagePathProvider.getProjectRootDirPath())
        val cacheDir = storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE)

        val instances = instancesRepository.getAllByStatus(
            Instance.STATUS_INCOMPLETE,
            Instance.STATUS_INVALID,
            Instance.STATUS_VALID
        )

        val result = instances.fold(FinalizeAllResult(0, 0, false)) { result, instance ->
            val (formDef, form) = FormEntryUseCases.loadFormDef(
                instance,
                formsRepository,
                projectRootDir,
                ExternalizableFormDefCache()
            )

            val formMediaDir = File(form.formMediaPath)
            val formEntryController =
                CollectFormEntryControllerFactory().create(formDef, formMediaDir)
            val formController = FormEntryUseCases.loadDraft(form, instance, formEntryController)

            val savePoint = FormEntryUseCases.getSavePoint(formController, File(cacheDir))
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

        update()
        instanceSubmitScheduler.scheduleSubmit(currentProjectProvider.getCurrentProject().uuid)

        return result.copy(successCount = instances.size - result.failureCount)
    }

    companion object {
        private const val EDITABLE_COUNT_KEY = "instancesEditableCount"
        private const val SENDABLE_COUNT_KEY = "instancesSendableCount"
        private const val SENT_COUNT_KEY = "instancesSentCount"
    }
}

data class FinalizeAllResult(val successCount: Int, val failureCount: Int, val unsupportedInstances: Boolean)
