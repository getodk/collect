package org.odk.collect.geo.geopoly

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.MapPoint

class GeoPolyViewModel(
    outputMode: OutputMode,
    points: List<MapPoint>,
    private val retainMockAccuracy: Boolean,
    private val locationTracker: LocationTracker,
    private val scheduler: Scheduler
) : ViewModel() {

    private val _points = MutableStateFlow(
        if (points.isNotEmpty()) {
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
    private var recording: Cancellable? = null

    fun add(point: MapPoint) {
        val points = _points.value
        if (points.isEmpty() || point != points.last()) {
            _points.value = points + point
        }
    }

    fun removeLast() {
        _points.value = _points.value.dropLast(1)
    }

    fun update(points: List<MapPoint>) {
        _points.value = points
    }

    fun startRecording(accuracyThreshold: Int, interval: Long) {
        this.accuracyThreshold = accuracyThreshold
        locationTracker.start(retainMockAccuracy)
        recording = scheduler.repeat({ recordPoint(accuracyThreshold) }, interval)
    }

    private fun recordPoint(accuracyThreshold: Int) {
        locationTracker.getLocation().value?.let {
            if (accuracyThreshold == 0 || it.accuracy <= accuracyThreshold) {
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

    fun stopRecording() {
        recording?.cancel()
        locationTracker.stop()
    }

    override fun onCleared() {
        stopRecording()
    }
}
