package org.odk.collect.android.injection.config

import org.odk.collect.googlemaps.GoogleMapsDependencyModule
import org.odk.collect.location.LocationClient
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider

class CollectGoogleMapsDependencyModule(
    private val appDependencyComponent: AppDependencyComponent
) : GoogleMapsDependencyModule() {
    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return appDependencyComponent.referenceLayerRepository()
    }

    override fun providesLocationClient(): LocationClient {
        return appDependencyComponent.locationClient()
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return appDependencyComponent.settingsProvider()
    }
}
