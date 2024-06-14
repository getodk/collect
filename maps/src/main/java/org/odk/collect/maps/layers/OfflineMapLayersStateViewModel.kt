package org.odk.collect.maps.layers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class OfflineMapLayersStateViewModel(
    private val settingsProvider: SettingsProvider
) : ViewModel() {
    private val _checkedLayerId =
        MutableLiveData<String?>(settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_REFERENCE_LAYER))
    val checkedLayerId: LiveData<String?> = _checkedLayerId

    fun onLayerChecked(layerId: String?) {
        _checkedLayerId.value = layerId
    }

    fun onLayersChanged(layerIds: List<String?>) {
        if (!layerIds.contains(_checkedLayerId.value)) {
            _checkedLayerId.value = null
            settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_REFERENCE_LAYER, null)
        }
    }

    fun getCheckedLayer(): String? {
        return checkedLayerId.value
    }
}
