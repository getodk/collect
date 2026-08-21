package org.odk.collect.geo.geopoint

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.odk.collect.maps.MapPoint

class GeoPointViewModel(inputPoint: MapPoint?) : ViewModel() {

    private val _isInputPoint = MutableStateFlow(inputPoint != null)
    val isInputPoint = _isInputPoint.asStateFlow()

    private val _geoPoint = MutableStateFlow(inputPoint)
    val geoPoint = _geoPoint.asStateFlow()

    fun place(point: MapPoint) {
        _geoPoint.value = point
    }

    fun hasGeoPoint(): Boolean {
        return _geoPoint.value != null
    }

    fun clear() {
        _geoPoint.value = null
        _isInputPoint.value = false
    }
}