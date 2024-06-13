package org.odk.collect.android.injection.config

import org.odk.collect.android.geo.MapConfiguratorProvider
import org.odk.collect.location.LocationClient
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.osmdroid.OsmDroidDependencyModule
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class CollectOsmDroidDependencyModule(
    private val appDependencyComponent: AppDependencyComponent
) : OsmDroidDependencyModule() {
    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return appDependencyComponent.referenceLayerRepository()
    }

    override fun providesLocationClient(): LocationClient {
        return appDependencyComponent.locationClient()
    }

    override fun providesMapConfigurator(): MapConfigurator {
        return MapConfiguratorProvider.getConfigurator(
            appDependencyComponent.settingsProvider().getUnprotectedSettings().getString(ProjectKeys.KEY_BASEMAP_SOURCE)
        )
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return appDependencyComponent.settingsProvider()
    }
}
