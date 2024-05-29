package org.odk.collect.maps.layers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class OfflineMapLayersPickerViewModel(
    private val referenceLayerRepository: ReferenceLayerRepository,
    scheduler: Scheduler,
    private val settingsProvider: SettingsProvider
) : ViewModel() {
    private val _data = MutableLiveData<Pair<List<ReferenceLayer>, String?>>()
    val data: LiveData<Pair<List<ReferenceLayer>, String?>> = _data

    init {
        scheduler.immediate(
            background = {
                val layers = referenceLayerRepository.getAll()
                val selectedLayerId = settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER)

                _data.postValue(Pair(layers, selectedLayerId))
            },
            foreground = { }
        )
    }

    fun saveSelectedLayer() {
        val selectedLayerId = data.value?.second
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, selectedLayerId)
    }

    fun changeSelectedLayerId(selectedLayerId: String?) {
        _data.postValue(_data.value?.copy(second = selectedLayerId))
    }
}
