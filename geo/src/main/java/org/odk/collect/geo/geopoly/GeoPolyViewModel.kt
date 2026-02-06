package org.odk.collect.geo.geopoly

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.androidshared.livedata.LiveDataExt.combine
import org.odk.collect.androidshared.livedata.LiveDataExt.withLast
import org.odk.collect.androidshared.ui.DisplayString
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.geopoly.GeoPolyFragment.OutputMode
import org.odk.collect.location.tracker.LocationTracker
import org.odk.collect.maps.MapPoint

class GeoPolyViewModel(
    outputMode: OutputMode,
    points: List<MapPoint>,
    retainMockAccuracy: Boolean,
    private val locationTracker: LocationTracker,
    private val scheduler: Scheduler,
    val invalidMessage: LiveData<DisplayString?>
) : ViewModel() {

    enum class RecordingMode {
        PLACEMENT, MANUAL, AUTOMATIC
    }

    var recordingMode: RecordingMode = RecordingMode.PLACEMENT
        private set

    var inputActive: Boolean = false
        private set

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

    val fixedAlerts = invalidMessage.withLast().map {
        if (it.second == null && it.first != null) {
            Consumable(Unit)
        } else {
            null
        }
    }

    val viewData = _points.asLiveData().combine(invalidMessage)

    private var accuracyThreshold: Int = 0
    private var recording: Cancellable? = null

    init {
        locationTracker.start(retainMockAccuracy)
    }

    fun add(point: MapPoint) {
        if (invalidMessage.value == null) {
            val points = _points.value
            if (points.isEmpty() || point != points.last()) {
                _points.value = points + point
            }
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
        recording = scheduler.repeat({ recordPoint(accuracyThreshold) }, interval)
    }

    fun recordPoint(accuracyThreshold: Int = 0) {
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
        disableInput()
        recording?.cancel()
        locationTracker.stop()
    }

    fun setRecordingMode(mode: RecordingMode) {
        recordingMode = mode
    }

    fun enableInput() {
        inputActive = true
    }

    fun disableInput() {
        inputActive = false
    }

    public override fun onCleared() {
        stopRecording()
    }
}
