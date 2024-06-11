package org.odk.collect.maps.layers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData

class OfflineMapLayersStateViewModel(checkedLayerId: String?) : ViewModel() {
    private val _expandedLayerIds = MutableNonNullLiveData<List<String?>>(emptyList())
    val expandedLayerIds: NonNullLiveData<List<String?>> = _expandedLayerIds

    private val _checkedLayerId = MutableLiveData<String?>(checkedLayerId)
    val checkedLayerId: LiveData<String?> = _checkedLayerId

    fun onLayerChecked(layerId: String?) {
        _checkedLayerId.postValue(layerId)
    }

    fun onLayerToggled(layerId: String?) {
        if (_expandedLayerIds.value.contains(layerId)) {
            _expandedLayerIds.postValue(_expandedLayerIds.value.filter { it != layerId })
        } else {
            _expandedLayerIds.postValue(_expandedLayerIds.value.plus(layerId))
        }
    }

    fun onLayerDeleted(layerId: String?) {
        _expandedLayerIds.postValue(_expandedLayerIds.value.filter { it != layerId })
    }

    fun getCheckedLayer(): String? {
        return checkedLayerId.value
    }
}
