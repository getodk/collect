package org.odk.collect.android.formlists.savedformlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.androidshared.livedata.LiveDataUtils.zip
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.instances.Instance

class SavedFormListViewModel(
    private val scheduler: Scheduler,
    private val instancesDataService: InstancesDataService
) : ViewModel() {

    private val _filterText = MutableLiveData("")
    var filterText: String = ""
        set(value) {
            field = value
            _filterText.value = value
        }

    val formsToDisplay: LiveData<List<Instance>> =
        zip(instancesDataService.instances.asLiveData(), _filterText)
            .map { (instances, filter) ->
                instances.filter {
                    it.displayName.contains(filter, ignoreCase = true)
                }
            }

    fun deleteForms(databaseIds: LongArray) {
        scheduler.immediate(background = true) {
            databaseIds.forEach { instancesDataService.deleteInstance(it) }
        }
    }
}
