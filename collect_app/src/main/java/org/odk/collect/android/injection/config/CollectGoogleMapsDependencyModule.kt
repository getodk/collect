package org.odk.collect.android.injection.config

import org.odk.collect.googlemaps.GoogleMapsDependencyModule
import org.odk.collect.location.LocationClient
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider

class CollectGoogleMapsDependencyModule(
    private val referenceLayerRepository: ReferenceLayerRepository,
    private val locationClient: LocationClient,
    private val settingsProvider: SettingsProvider
) : GoogleMapsDependencyModule() {
    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return referenceLayerRepository
    }

    override fun providesLocationClient(): LocationClient {
        return locationClient
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return settingsProvider
    }
}
