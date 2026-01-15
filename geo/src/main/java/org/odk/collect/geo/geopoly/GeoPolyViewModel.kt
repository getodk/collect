package org.odk.collect.geo.geopoly

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode
import org.odk.collect.maps.MapPoint
import kotlin.collections.plus

class GeoPolyViewModel(outputMode: OutputMode, points: List<MapPoint>) :
    ViewModel() {

    private val _points = MutableStateFlow(
        if (!points.isEmpty()) {
            if (outputMode == OutputMode.GEOSHAPE) {
                points.subList(0, points.size - 1)
            } else {
                points
            }
        } else {
            points
        }
    )
    val points: StateFlow<List<MapPoint>> = _points

    fun add(point: MapPoint) {
        val points = _points.value
        if (points.isEmpty() || point != points[points.size - 1]) {
            _points.value = points + point
        }
    }

    fun removeLast() {
        _points.value = _points.value.dropLast(1)
    }

    fun update(points: List<MapPoint>) {
        _points.value = points
    }
}
