package org.odk.collect.android.injection.config

import org.odk.collect.maplibre.MapLibreDependencyModule
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider

class CollectMapLibreDependencyModule(
    private val appDependencyComponent: AppDependencyComponent
) : MapLibreDependencyModule() {
    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return appDependencyComponent.referenceLayerRepository()
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return appDependencyComponent.settingsProvider()
    }
}
