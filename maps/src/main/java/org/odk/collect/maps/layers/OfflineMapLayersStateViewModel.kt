package org.odk.collect.maps.layers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class OfflineMapLayersStateViewModel(settingsProvider: SettingsProvider) : ViewModel() {
    private val _expandedLayerIds = MutableNonNullLiveData<List<String?>>(emptyList())
    val expandedLayerIds: NonNullLiveData<List<String?>> = _expandedLayerIds

    private val _checkedLayerId =
        MutableLiveData<String?>(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER))
    val checkedLayerId: LiveData<String?> = _checkedLayerId

    fun onLayerChecked(layerId: String?) {
        _checkedLayerId.value = layerId
    }

    fun onLayerToggled(layerId: String?) {
        if (_expandedLayerIds.value.contains(layerId)) {
            _expandedLayerIds.value = _expandedLayerIds.value.filter { it != layerId }
        } else {
            _expandedLayerIds.value = _expandedLayerIds.value.plus(layerId)
        }
    }

    fun onLayerDeleted(layerId: String?) {
        _expandedLayerIds.value = _expandedLayerIds.value.filter { it != layerId }
    }

    fun getCheckedLayer(): String? {
        return checkedLayerId.value
    }
}
