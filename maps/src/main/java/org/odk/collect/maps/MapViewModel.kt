package org.odk.collect.maps

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.settings.keys.MetaKeys.LAST_KNOWN_ZOOM_LEVEL
import org.odk.collect.shared.settings.Settings

class MapViewModel(
    private val configurator: MapConfigurator,
    private val unprotectedSettings: Settings,
    private val metaSettings: Settings
) : ViewModel(),
    Settings.OnSettingChangeListener {

    private var userZoomLevel: Double? = null

    private val _zoom = MutableLiveData<Zoom?>()
    val zoom: LiveData<Zoom?> = _zoom

    private val _config = MutableLiveData(configurator.buildConfig(unprotectedSettings))
    val config: LiveData<Bundle> = _config

    init {
        unprotectedSettings.registerOnSettingChangeListener(this)
    }

    fun zoomTo(point: MapPoint?, level: Double?, animate: Boolean) {
        if (point != null) {
            _zoom.value = Zoom.Point(point, level ?: DEFAULT_ZOOM, animate, false)
        }
    }

    fun zoomTo(boundingBox: List<MapPoint>, level: Double, animate: Boolean) {
        _zoom.value = Zoom.Box(boundingBox, level, animate)
    }

    fun zoomToCurrentLocation(location: MapPoint?) {
        if (location == null) {
            return
        }

        val level = if (metaSettings.contains(LAST_KNOWN_ZOOM_LEVEL)) {
            metaSettings.getFloat(LAST_KNOWN_ZOOM_LEVEL).toDouble()
        } else {
            DEFAULT_ZOOM
        }

        zoomTo(location, level, true)
    }

    fun moveTo(location: MapPoint?, animate: Boolean) {
        if (location != null) {
            _zoom.value = Zoom.Point(location, _zoom.value?.level, animate, false)
        }
    }

    fun onUserMove(point: MapPoint, level: Double) {
        userZoomLevel = level
        _zoom.value = Zoom.Point(point, level, animate = false, user = true)
    }

    override fun onCleared() {
        userZoomLevel?.also {
            metaSettings.save(LAST_KNOWN_ZOOM_LEVEL, it.toFloat())
        }

        unprotectedSettings.unregisterOnSettingChangeListener(this)
    }

    override fun onSettingChanged(key: String) {
        if (configurator.prefKeys.contains(key)) {
            _config.value = configurator.buildConfig(unprotectedSettings)
        }
    }

    companion object {
        const val DEFAULT_ZOOM: Double = 16.0
    }
}

sealed class Zoom {

    abstract val level: Double?
    abstract val animate: Boolean
    abstract val user: Boolean

    data class Point(
        val point: MapPoint,
        override val level: Double?,
        override val animate: Boolean,
        override val user: Boolean
    ) : Zoom()

    data class Box(
        val box: List<MapPoint>,
        override val level: Double?,
        override val animate: Boolean
    ) : Zoom() {
        override val user = false
    }
}
