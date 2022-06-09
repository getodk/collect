package org.odk.collect.android.injection.config

import org.odk.collect.androidshared.network.NetworkStateProvider
import org.odk.collect.mapbox.MapboxDependencyModule
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider

class CollectMapboxDependencyModule(
    private val settingsProvider: SettingsProvider,
    private val networkStateProvider: NetworkStateProvider,
    private val referenceLayerRepository: ReferenceLayerRepository
) : MapboxDependencyModule() {

    override fun providesSettingsProvider(): SettingsProvider {
        return settingsProvider
    }

    override fun providesNetworkStateProvider(): NetworkStateProvider {
        return networkStateProvider
    }

    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return referenceLayerRepository
    }
}
