package org.odk.collect.android.formmanagement.drafts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.formmanagement.InstancesDataService
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.async.Scheduler

class BulkFinalizationViewModel(
    private val scheduler: Scheduler,
    private val instancesDataService: InstancesDataService,
    private val currentProjectProvider: CurrentProjectProvider
) {
    private val _finalizedForms = MutableLiveData<Consumable<Int>>()
    val finalizedForms: LiveData<Consumable<Int>> = _finalizedForms

    fun finalizeAllDrafts() {
        scheduler.immediate(
            background = {
                instancesDataService.finalizeAllDrafts(currentProjectProvider.getCurrentProject().uuid)
            },
            foreground = {
                _finalizedForms.value = Consumable(it)
            }
        )
    }
}
