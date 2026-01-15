package org.odk.collect.geo.geopoly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.MapPoint

class GeoPolyViewModel(
    outputMode: OutputMode,
    points: List<MapPoint>,
    private val locationTracker: LocationTracker
) :
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
    private var accuracyThreshold: Int = 0

    init {
        viewModelScope.launch {
            locationTracker.getLocation().collect {
                if (it != null) {
                    accuracyThreshold.let { threshold ->
                        if (threshold == 0 || it.accuracy <= threshold) {
                            add(
                                MapPoint(
                                    it.latitude,
                                    it.longitude,
                                    it.altitude,
                                    it.accuracy.toDouble()
                                )
                            )
                        }
                    }
                }
            }
        }
    }

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

    fun startRecording(retainMockAccuracy: Boolean, accuracyThreshold: Int, interval: Long) {
        this.accuracyThreshold = accuracyThreshold
        locationTracker.start(retainMockAccuracy, interval)
    }

    fun stopRecording() {
        locationTracker.stop()
    }

    override fun onCleared() {
        locationTracker.stop()
    }
}
