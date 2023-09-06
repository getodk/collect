package org.odk.collect.android.formmanagement.drafts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.formentry.FormEntryUseCases.finalizeDraft
import org.odk.collect.android.formentry.FormEntryUseCases.loadDraft
import org.odk.collect.android.formentry.FormEntryUseCases.loadFormDef
import org.odk.collect.android.tasks.FormLoaderTask.FormEntryControllerFactory
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.async.Scheduler
import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.InstancesRepository
import java.io.File

class BulkFinalizationViewModel(
    private val scheduler: Scheduler,
    private val instancesRepository: InstancesRepository,
    private val formsRepository: FormsRepository,
    private val entitiesRepository: EntitiesRepository,
    private val formEntryControllerFactory: FormEntryControllerFactory
) {
    private val _finalizedForms = MutableLiveData<Consumable<Int>>()
    val finalizedForms: LiveData<Consumable<Int>> = _finalizedForms

    fun finalizeAllDrafts() {
        scheduler.immediate(
            background = {
                val instances = instancesRepository.all

                instances.forEach {
                    val form = formsRepository.getAllByFormId(it.formId)[0]
                    val xForm = File(form.formFilePath)
                    val formMediaDir = FileUtils.getFormMediaDir(xForm)
                    val formDef = loadFormDef(xForm, formMediaDir)!!

                    val formEntryController = formEntryControllerFactory.create(formDef)
                    val instanceFile = File(it.instanceFilePath)
                    val formController = loadDraft(formEntryController, formMediaDir, instanceFile)

                    finalizeDraft(formController, entitiesRepository, instancesRepository)
                }

                instances.size
            },
            foreground = {
                _finalizedForms.value = Consumable(it)
            }
        )
    }
}
