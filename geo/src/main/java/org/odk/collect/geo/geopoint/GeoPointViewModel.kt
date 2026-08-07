package org.odk.collect.geo.geopoint

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.odk.collect.maps.MapPoint

class GeoPointViewModel(inputPoint: MapPoint?) : ViewModel() {

    private val _geopoint = MutableStateFlow(inputPoint)
    val geoPoint: StateFlow<MapPoint?> = _geopoint

    fun place(point: MapPoint) {
        _geopoint.value = point
    }

    fun clear() {
        _geopoint.value = null
    }
}