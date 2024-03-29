package org.odk.collect.android.formlists.savedformlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.androidshared.async.TrackableWorker
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.async.Scheduler
import org.odk.collect.async.flowOnBackground
import org.odk.collect.forms.instances.Instance
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings

class SavedFormListViewModel(
    scheduler: Scheduler,
    private val settings: Settings,
    private val instancesDataService: InstancesDataService
) : ViewModel() {

    private val _sortOrder =
        MutableStateFlow(SortOrder.entries[settings.getInt(ProjectKeys.KEY_SAVED_FORM_SORT_ORDER)])
    var sortOrder: SortOrder = _sortOrder.value
        set(value) {
            settings.save(ProjectKeys.KEY_SAVED_FORM_SORT_ORDER, value.ordinal)
            _sortOrder.value = value
            field = value
        }

    private val _filterText = MutableStateFlow("")
    var filterText: String = ""
        set(value) {
            field = value
            _filterText.value = value
        }

    val formsToDisplay: LiveData<List<Instance>> = instancesDataService.instances
        .combine(_sortOrder) { instances, order ->
            when (order) {
                SortOrder.NAME_DESC -> {
                    instances.sortedByDescending { it.displayName }
                }

                SortOrder.DATE_DESC -> {
                    instances.sortedByDescending { it.lastStatusChangeDate }
                }

                SortOrder.NAME_ASC -> {
                    instances.sortedBy { it.displayName }
                }

                SortOrder.DATE_ASC -> {
                    instances.sortedBy { it.lastStatusChangeDate }
                }
            }
        }.combine(_filterText) { instances, filter ->
            instances.filter { it.displayName.contains(filter, ignoreCase = true) }
        }.flowOnBackground(scheduler).asLiveData()

    private val worker = TrackableWorker(scheduler)
    val isDeleting: LiveData<Boolean> = worker.isWorking

    fun deleteForms(databaseIds: LongArray): LiveData<Consumable<Int>?> {
        val result = MutableLiveData<Consumable<Int>?>(null)
        worker.immediate(
            background = {
                instancesDataService.deleteInstances(databaseIds)
            },
            foreground = { instancesDeleted ->
                if (instancesDeleted) {
                    result.value = Consumable(databaseIds.count())
                } else {
                    result.value = Consumable(0)
                }
            }
        )

        return result
    }

    enum class SortOrder {
        NAME_ASC,
        NAME_DESC,
        DATE_DESC,
        DATE_ASC
    }
}
