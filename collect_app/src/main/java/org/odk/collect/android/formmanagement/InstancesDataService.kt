package org.odk.collect.android.formmanagement

import androidx.lifecycle.LiveData
import org.odk.collect.android.application.Collect
import org.odk.collect.android.entities.EntitiesRepositoryProvider
import org.odk.collect.android.formentry.FormEntryUseCases
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

    fun finalizeAllDrafts(): Pair<Int, Int> {
        val instancesRepository = instancesRepositoryProvider.get()
        val formsRepository = formsRepositoryProvider.get()
        val entitiesRepository = entitiesRepositoryProvider.get()
        val projectRootDir = File(storagePathProvider.getProjectRootDirPath())

        val instances = instancesRepository.getAllByStatus(
            Instance.STATUS_INCOMPLETE,
            Instance.STATUS_INVALID,
            Instance.STATUS_VALID
        )

        val totalFailed = instances.fold(0) { failCount, instance ->
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

            val cacheDir = storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE)
            val savePoint = FormEntryUseCases.getSavePoint(formController, File(cacheDir))
            val newFailCount = if (savePoint == null && form.basE64RSAPublicKey == null) {
                val finalizedInstance = FormEntryUseCases.finalizeDraft(
                    formController,
                    instancesRepository,
                    entitiesRepository
                )

                if (finalizedInstance == null) {
                    failCount + 1
                } else {
                    failCount
                }
            } else {
                failCount + 1
            }

            Collect.getInstance().externalDataManager?.close()
            newFailCount
        }

        update()
        return Pair(instances.size, totalFailed)
    }

    companion object {
        private const val EDITABLE_COUNT_KEY = "instancesEditableCount"
        private const val SENDABLE_COUNT_KEY = "instancesSendableCount"
        private const val SENT_COUNT_KEY = "instancesSentCount"
    }
}
