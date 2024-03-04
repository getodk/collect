package org.odk.collect.entities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.async.Scheduler

class EntitiesViewModel(
    private val scheduler: Scheduler,
    private val entitiesRepository: EntitiesRepository
) : ViewModel() {

    private val _datasets = MutableLiveData<List<String>>(emptyList())
    val datasets: LiveData<List<String>> = _datasets

    init {
        scheduler.immediate(background = true) {
            _datasets.postValue(entitiesRepository.getDatasets().toList())
        }
    }

    fun getEntities(dataset: String): LiveData<List<Entity>> {
        val result = MutableLiveData<List<Entity>>(emptyList())
        scheduler.immediate(background = true) {
            result.postValue(entitiesRepository.getEntities(dataset))
        }

        return result
    }
}
