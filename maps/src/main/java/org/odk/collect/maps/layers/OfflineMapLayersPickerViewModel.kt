package org.odk.collect.maps.layers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
                val layers = referenceLayerRepository.getAllSupported()
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

    open class Factory(
        private val referenceLayerRepository: ReferenceLayerRepository,
        private val scheduler: Scheduler,
        private val settingsProvider: SettingsProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OfflineMapLayersPickerViewModel(referenceLayerRepository, scheduler, settingsProvider) as T
        }
    }
}
