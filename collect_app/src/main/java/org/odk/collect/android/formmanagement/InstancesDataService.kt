package org.odk.collect.android.formmanagement

import android.content.Context
import androidx.lifecycle.LiveData
import org.odk.collect.android.entities.EntitiesRepositoryProvider
import org.odk.collect.android.formentry.FormEntryUseCases
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.androidshared.data.getState
import org.odk.collect.forms.instances.Instance
import java.io.File

class InstancesDataService(
    context: Context,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val entitiesRepositoryProvider: EntitiesRepositoryProvider,
    private val onUpdate: () -> Unit
) {
    private val appState = context.getState()

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

        val editableInstances = instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE)

        appState.setLive(EDITABLE_COUNT_KEY, editableInstances)
        appState.setLive(SENDABLE_COUNT_KEY, sendableInstances)
        appState.setLive(SENT_COUNT_KEY, sentInstances)

        onUpdate()
    }

    fun finalizeAllDrafts(): Int {
        val instancesRepository = instancesRepositoryProvider.get()
        val formsRepository = formsRepositoryProvider.get()
        val entitiesRepository = entitiesRepositoryProvider.get()

        val instances = instancesRepository.all

        instances.forEach {
            val form = formsRepository.getAllByFormId(it.formId)[0]
            val xForm = File(form.formFilePath)
            val formMediaDir = FileUtils.getFormMediaDir(xForm)
            val formDef = FormEntryUseCases.loadFormDef(xForm, formMediaDir)!!

            val formEntryController = CollectFormEntryControllerFactory().create(formDef)
            val instanceFile = File(it.instanceFilePath)
            val formController =
                FormEntryUseCases.loadDraft(formEntryController, formMediaDir, instanceFile)

            FormEntryUseCases.finalizeDraft(formController, entitiesRepository, instancesRepository)
        }

        update()
        return instances.size
    }

    companion object {
        private const val EDITABLE_COUNT_KEY = "instancesEditableCount"
        private const val SENDABLE_COUNT_KEY = "instancesSendableCount"
        private const val SENT_COUNT_KEY = "instancesSentCount"
    }
}
