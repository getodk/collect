package org.odk.collect.android.injection.config

import android.content.Context
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.R
import org.odk.collect.maplibre.MapLibreDependencyModule
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.shared.injection.Keys

class CollectMapLibreDependencyModule(
    private val context: Context,
    private val appDependencyComponent: AppDependencyComponent
) : MapLibreDependencyModule() {
    override fun providesReferenceLayerRepository(): ReferenceLayerRepository {
        return appDependencyComponent.referenceLayerRepository()
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return appDependencyComponent.settingsProvider()
    }

    override fun providesKeys(): Keys {
        return Keys(
            mapOf(
                "carto" to BuildConfig.CARTO_API_KEY,
                "google" to context.getString(R.string.GOOGLE_MAPS_API_KEY)
            )
        )
    }
}
