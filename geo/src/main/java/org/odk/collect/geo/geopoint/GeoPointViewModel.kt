package org.odk.collect.geo.geopoint

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.odk.collect.maps.MapPoint

class GeoPointViewModel(inputPoint: MapPoint?) : ViewModel() {

    private val _geoPoint = MutableStateFlow(inputPoint)
    val geoPoint: StateFlow<MapPoint?> = _geoPoint

    fun place(point: MapPoint) {
        _geoPoint.value = point
    }

    fun clear() {
        _geoPoint.value = null
    }
}