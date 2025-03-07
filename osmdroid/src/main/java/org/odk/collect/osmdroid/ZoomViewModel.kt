package org.odk.collect.osmdroid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.maps.MapPoint
import org.odk.collect.settings.keys.MetaKeys.LAST_KNOWN_ZOOM_LEVEL
import org.odk.collect.shared.settings.Settings

class ZoomViewModel(private val settings: Settings) : ViewModel() {

    private var userZoomLevel: Double? = null

    private val _zoom = MutableLiveData<Zoom?>()
    val zoom: LiveData<Zoom?> = _zoom

    fun zoomTo(point: MapPoint, level: Double?, animate: Boolean) {
        _zoom.value = Zoom.Point(point, level ?: DEFAULT_ZOOM, animate, false)
    }

    fun zoomTo(boundingBox: List<MapPoint>, level: Double, animate: Boolean) {
        _zoom.value = Zoom.Box(boundingBox, level, animate)
    }

    fun zoomToCurrentLocation(location: MapPoint) {
        val level = if (settings.contains(LAST_KNOWN_ZOOM_LEVEL)) {
            settings.getFloat(LAST_KNOWN_ZOOM_LEVEL).toDouble()
        } else {
            DEFAULT_ZOOM
        }

        zoomTo(location, level, true)
    }

    fun moveTo(location: MapPoint, animate: Boolean) {
        _zoom.value = Zoom.Point(location, _zoom.value?.level, animate, false)
    }

    fun onUserMove(point: MapPoint, level: Double) {
        userZoomLevel = level
        _zoom.value = Zoom.Point(point, level, animate = false, user = true)
    }

    override fun onCleared() {
        userZoomLevel?.also {
            settings.save(LAST_KNOWN_ZOOM_LEVEL, it.toFloat())
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
