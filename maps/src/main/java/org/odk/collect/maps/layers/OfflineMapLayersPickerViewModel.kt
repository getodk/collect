package org.odk.collect.maps.layers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.async.Scheduler
import org.odk.collect.settings.SettingsProvider

class OfflineMapLayersPickerViewModel(
    private val referenceLayerRepository: ReferenceLayerRepository,
    private val scheduler: Scheduler,
    private val settingsProvider: SettingsProvider
) : ViewModel() {

    class Factory(
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
