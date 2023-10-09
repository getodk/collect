package org.odk.collect.android.formmanagement

import androidx.lifecycle.LiveData
import org.odk.collect.android.entities.EntitiesRepositoryProvider
import org.odk.collect.android.formentry.FormEntryUseCases
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.utilities.FileUtils
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
            val form = formsRepository.getAllByFormId(instance.formId)[0]
            val xForm = File(form.formFilePath)
            val formMediaDir = FileUtils.getFormMediaDir(xForm)
            val formDef = FormEntryUseCases.loadFormDef(xForm, projectRootDir, formMediaDir)!!

            val formEntryController = CollectFormEntryControllerFactory().create(formDef)
            val instanceFile = File(instance.instanceFilePath)
            val formController =
                FormEntryUseCases.loadDraft(formEntryController, formMediaDir, instanceFile)

            val instance = FormEntryUseCases.finalizeDraft(
                formController,
                instancesRepository,
                entitiesRepository
            )

            if (instance == null) {
                failCount + 1
            } else {
                failCount
            }
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
