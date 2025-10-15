package org.odk.collect.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.settings.keys.MetaKeys.LAST_KNOWN_ZOOM_LEVEL
import org.odk.collect.shared.settings.Settings

class MapViewModel(
    private val unprotectedSettings: Settings,
    private val metaSettings: Settings
) : ViewModel(),
    Settings.OnSettingChangeListener {

    private var userZoomLevel: Double? = null

    private val _zoom = MutableLiveData<Zoom?>()
    val zoom: LiveData<Zoom?> = _zoom

    private val lastSettingsKeyChange = MutableLiveData<String>(null)

    init {
        unprotectedSettings.registerOnSettingChangeListener(this)
    }

    fun zoomTo(point: MapPoint?, level: Double?, animate: Boolean) {
        if (point != null) {
            _zoom.value = Zoom.Point(point, level ?: DEFAULT_ZOOM, animate, false)
        }
    }

    fun zoomTo(boundingBox: List<MapPoint>, scaleFactor: Double, animate: Boolean) {
        _zoom.value = Zoom.Box(boundingBox.distinct(), scaleFactor, _zoom.value?.level ?: DEFAULT_ZOOM, animate)
    }

    fun zoomToCurrentLocation(location: MapPoint?) {
        if (location == null) {
            return
        }

        val level = if (userZoomLevel != null) {
            userZoomLevel
        } else if (metaSettings.contains(LAST_KNOWN_ZOOM_LEVEL)) {
            metaSettings.getFloat(LAST_KNOWN_ZOOM_LEVEL).toDouble()
        } else {
            DEFAULT_ZOOM
        }

        zoomTo(location, level, true)
    }

    fun moveTo(location: MapPoint?, animate: Boolean) {
        if (location != null) {
            _zoom.value = Zoom.Point(location, _zoom.value?.level ?: DEFAULT_ZOOM, animate, false)
        }
    }

    fun onUserZoom(point: MapPoint, level: Double) {
        userZoomLevel = level
        _zoom.value = Zoom.Point(point, level, animate = false, user = true)
    }

    fun onUserMove(point: MapPoint, level: Double) {
        _zoom.value = Zoom.Point(point, level, animate = false, user = true)
    }

    fun getSettings(keys: Collection<String>): LiveData<Settings> {
        return MediatorLiveData<Settings>().apply {
            addSource(lastSettingsKeyChange) {
                if (it == null || keys.contains(it)) {
                    this.value = unprotectedSettings
                }
            }
        }
    }

    override fun onCleared() {
        userZoomLevel?.let {
            metaSettings.save(LAST_KNOWN_ZOOM_LEVEL, it.toFloat())
        }

        unprotectedSettings.unregisterOnSettingChangeListener(this)
    }

    override fun onSettingChanged(key: String) {
        lastSettingsKeyChange.value = key
    }

    companion object {
        const val DEFAULT_ZOOM: Double = 16.0
    }
}

sealed class Zoom {

    abstract val level: Double
    abstract val animate: Boolean
    abstract val user: Boolean

    data class Point(
        val point: MapPoint,
        override val level: Double,
        override val animate: Boolean,
        override val user: Boolean
    ) : Zoom()

    data class Box(
        val box: List<MapPoint>,
        val scaleFactor: Double,
        override val level: Double,
        override val animate: Boolean
    ) : Zoom() {
        override val user = false
    }
}
