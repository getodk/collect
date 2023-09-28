package org.odk.collect.android.application.initialization

import android.content.Context
import android.os.Handler
import com.google.android.gms.maps.MapView
import org.odk.collect.android.geo.MapConfiguratorProvider
import org.odk.collect.osmdroid.OsmDroidInitializer
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.utilities.UserAgentProvider
import timber.log.Timber
import javax.inject.Inject

class MapsInitializer @Inject constructor(
    private val context: Context,
    private val settingsProvider: SettingsProvider,
    private val userAgentProvider: UserAgentProvider
) {

    fun initialize() {
        resetToAvailableFramework()
        initializeFrameworks()
    }

    private fun resetToAvailableFramework() {
        MapConfiguratorProvider.initOptions(context)
        val availableBaseMaps = MapConfiguratorProvider.getIds()
        val baseMapSetting =
            settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_BASEMAP_SOURCE)
        if (!availableBaseMaps.contains(baseMapSetting)) {
            settingsProvider.getUnprotectedSettings().save(
                ProjectKeys.KEY_BASEMAP_SOURCE,
                availableBaseMaps[0]
            )
        }
    }

    private fun initializeFrameworks() {
        try {
            com.google.android.gms.maps.MapsInitializer.initialize(
                context,
                com.google.android.gms.maps.MapsInitializer.Renderer.LATEST
            ) { renderer: com.google.android.gms.maps.MapsInitializer.Renderer ->
                when (renderer) {
                    com.google.android.gms.maps.MapsInitializer.Renderer.LATEST -> Timber.d("The latest version of Google Maps renderer is used.")
                    com.google.android.gms.maps.MapsInitializer.Renderer.LEGACY -> Timber.d("The legacy version of Google Maps renderer is used.")
                }
            }
            val handler = Handler(context.mainLooper)
            handler.post {
                // This has to happen on the main thread but we might call `initialize` from tests
                MapView(context).onCreate(null)
            }
            OsmDroidInitializer.initialize(userAgentProvider.userAgent)
        } catch (ignore: Exception) {
            // ignored
        } catch (ignore: Error) {
            // ignored
        }
    }
}
