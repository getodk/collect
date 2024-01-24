package org.odk.collect.android.formlists.savedformlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.instances.Instance

class SavedFormListViewModel(
    private val scheduler: Scheduler,
    private val instancesDataService: InstancesDataService
) : ViewModel() {

    val formsToDisplay: LiveData<List<Instance>> = instancesDataService.instances.asLiveData()

    fun deleteForms(databaseIds: LongArray) {
        scheduler.immediate(background = true) {
            databaseIds.forEach { instancesDataService.deleteInstance(it) }
        }
    }
}
