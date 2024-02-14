package org.odk.collect.android.formlists.savedformlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.async.Scheduler
import org.odk.collect.async.flowOnBackground
import org.odk.collect.forms.instances.Instance

class SavedFormListViewModel(
    private val scheduler: Scheduler,
    private val instancesDataService: InstancesDataService
) : ViewModel() {

    private val _filterText = MutableStateFlow("")
    var filterText: String = ""
        set(value) {
            field = value
            _filterText.value = value
        }

    val formsToDisplay: LiveData<List<Instance>> = instancesDataService.instances
        .combine(_filterText) { instances, filter ->
            instances.filter { it.displayName.contains(filter, ignoreCase = true) }
        }.flowOnBackground(scheduler).asLiveData()

    fun deleteForms(databaseIds: LongArray) {
        scheduler.immediate(background = true) {
            databaseIds.forEach { instancesDataService.deleteInstance(it) }
        }
    }
}
