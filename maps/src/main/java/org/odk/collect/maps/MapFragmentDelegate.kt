package org.odk.collect.maps

import android.os.Bundle
import org.odk.collect.shared.settings.Settings
import org.odk.collect.shared.settings.Settings.OnSettingChangeListener
import java.util.function.Consumer

class MapFragmentDelegate(
    private val mapFragment: MapFragment,
    configuratorProvider: () -> MapConfigurator,
    settingsProvider: () -> Settings,
    private val onConfigChanged: Consumer<Bundle>,
) : OnSettingChangeListener {

    private val configurator by lazy { configuratorProvider() }
    private val settings by lazy { settingsProvider() }

    private var savedInstanceState: Bundle? = null

    fun onCreate(savedInstanceState: Bundle?) {
        this.savedInstanceState = savedInstanceState
    }

    fun onReady() {
        val mapCenter: MapPoint? = savedInstanceState?.getParcelable(MAP_CENTER_KEY)
        val mapZoom: Double? = savedInstanceState?.getDouble(MAP_ZOOM_KEY)

        if (mapCenter != null && mapZoom != null) {
            mapFragment.zoomToPoint(mapCenter, mapZoom, false)
        } else if (mapCenter != null) {
            mapFragment.zoomToPoint(mapCenter, false)
        }
    }

    fun onStart() {
        onConfigChanged.accept(configurator.buildConfig(settings))
        settings.registerOnSettingChangeListener(this)
    }

    fun onStop() {
        settings.unregisterOnSettingChangeListener(this)
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(MAP_CENTER_KEY, mapFragment.center)
        outState.putDouble(MAP_ZOOM_KEY, mapFragment.zoom)
    }

    override fun onSettingChanged(key: String) {
        if (configurator.prefKeys.contains(key)) {
            onConfigChanged.accept(configurator.buildConfig(settings))
        }
    }

    companion object {
        private const val MAP_CENTER_KEY = "map_center"
        private const val MAP_ZOOM_KEY = "map_zoom"
    }
}
