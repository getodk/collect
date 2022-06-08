package org.odk.collect.android.injection.config

import org.odk.collect.android.geo.MapConfiguratorProvider
import org.odk.collect.location.LocationClient
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.osmdroid.OsmDroidDependencyModule
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class CollectOsmDroidDependencyModule(
    private val referenceLayerRepository: ReferenceLayerRepository,
    private val locationClient: LocationClient,
    private val settingsProvider: SettingsProvider,
) : OsmDroidDependencyModule() {
    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return referenceLayerRepository
    }

    override fun providesLocationClient(): LocationClient {
        return locationClient
    }

    override fun providesMapConfigurator(): MapConfigurator {
        return MapConfiguratorProvider.getConfigurator(
            settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_BASEMAP_SOURCE)
        )
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return settingsProvider
    }
}
