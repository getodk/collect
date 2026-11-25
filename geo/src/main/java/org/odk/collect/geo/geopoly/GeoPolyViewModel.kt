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
import org.odk.collect.settings.keys.MetaKeys
import org.odk.collect.shared.settings.Settings

class GeoPolyViewModel(
    outputMode: OutputMode,
    points: List<MapPoint>,
    retainMockAccuracy: Boolean,
    private val locationTracker: LocationTracker,
    private val scheduler: Scheduler,
    private val metaSettings: Settings,
    val invalidMessage: LiveData<DisplayString?>
) : ViewModel() {

    enum class RecordingMode {
        PLACEMENT, MANUAL, AUTOMATIC
    }

    var recordingMode: RecordingMode = metaSettings.getString(MetaKeys.LAST_USED_GEOPOLY_RECORDING_MODE)
        ?.let { saved -> RecordingMode.entries.find { it.name == saved } }
        ?: RecordingMode.PLACEMENT
        private set

    var intervalIndex: Int = getLastUsedIndex(
        MetaKeys.LAST_USED_GEOPOLY_INTERVAL, INTERVAL_OPTIONS, DEFAULT_INTERVAL_INDEX
    )
        private set

    var accuracyThresholdIndex: Int = getLastUsedIndex(
        MetaKeys.LAST_USED_GEOPOLY_ACCURACY_THRESHOLD, ACCURACY_THRESHOLD_OPTIONS, DEFAULT_ACCURACY_THRESHOLD_INDEX
    )
        private set

    private val _inputActive = MutableStateFlow(false)
    var inputActive: StateFlow<Boolean> = _inputActive
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

    val geoPoly = _points.asLiveData()
        .combine(invalidMessage)
        .map {
            GeoPoly(it.first ?: emptyList(), it.second == null)
        }

    val currentLocation = locationTracker.getLocation()

    private var accuracyThreshold: Int = 0
    private var recording: Cancellable? = null

    init {
        locationTracker.start(retainMockAccuracy)
    }

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
    }

    fun setRecordingMode(mode: RecordingMode) {
        recordingMode = mode
        metaSettings.save(MetaKeys.LAST_USED_GEOPOLY_RECORDING_MODE, mode.name)
    }

    fun setIntervalIndex(intervalIndex: Int) {
        this.intervalIndex = intervalIndex
        metaSettings.save(MetaKeys.LAST_USED_GEOPOLY_INTERVAL, INTERVAL_OPTIONS[intervalIndex])
    }

    fun setAccuracyThresholdIndex(accuracyThresholdIndex: Int) {
        this.accuracyThresholdIndex = accuracyThresholdIndex
        metaSettings.save(MetaKeys.LAST_USED_GEOPOLY_ACCURACY_THRESHOLD, ACCURACY_THRESHOLD_OPTIONS[accuracyThresholdIndex])
    }

    fun enableInput() {
        _inputActive.value = true
    }

    fun disableInput() {
        _inputActive.value = false
    }

    public override fun onCleared() {
        stopRecording()
        locationTracker.stop()
    }

    private fun getLastUsedIndex(key: String, options: IntArray, defaultIndex: Int): Int {
        if (!metaSettings.contains(key)) {
            return defaultIndex
        }

        val index = options.indexOf(metaSettings.getInt(key))
        return if (index != -1) index else defaultIndex
    }

    data class GeoPoly(val points: List<MapPoint>, val isValid: Boolean)

    companion object {
        val INTERVAL_OPTIONS = intArrayOf(
            1, 5, 10, 20, 30, 60, 300, 600, 1200, 1800
        )
        const val DEFAULT_INTERVAL_INDEX = 3 // default is 20 seconds

        val ACCURACY_THRESHOLD_OPTIONS = intArrayOf(
            0, 3, 5, 10, 15, 20
        )
        private const val DEFAULT_ACCURACY_THRESHOLD_INDEX = 3 // default is 10 meters
    }
}
