package org.odk.collect.geo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.analytics.Analytics.Companion.log
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.analytics.AnalyticsEvents
import org.odk.collect.location.Location
import org.odk.collect.location.SatelliteInfoClient
import org.odk.collect.location.tracker.LocationTracker

internal abstract class GeoPointViewModel : ViewModel() {

    abstract val accuracyThreshold: Float

    abstract val acceptedLocation: LiveData<Location?>
    abstract val currentAccuracy: LiveData<Float?>
    abstract val timeElapsed: LiveData<Long>
    abstract val satellites: NonNullLiveData<Int>

    abstract fun start(retainMockAccuracy: Boolean = false, accuracyThreshold: Float? = null)
    abstract fun forceLocation()
}

internal class LocationTrackerGeoPointViewModel(
    private val locationTracker: LocationTracker,
    private val satelliteInfoClient: SatelliteInfoClient,
    private val clock: () -> Long,
    scheduler: Scheduler
) : GeoPointViewModel() {

    private val startTime = clock()
    private val repeat = scheduler.repeat(
        {
            _timeElapsed.value = clock() - startTime
            updateLocation()
        },
        1000L
    )

    override var accuracyThreshold: Float = Float.MAX_VALUE
        private set

    private val trackerLocation = MutableLiveData<Location?>(null)
    private val _acceptedLocation = MutableLiveData<Location?>(null)
    override val acceptedLocation = _acceptedLocation

    override val currentAccuracy = Transformations.map(trackerLocation) {
        it?.accuracy
    }

    private val _timeElapsed = MutableLiveData<Long>(0)
    override val timeElapsed = _timeElapsed

    override val satellites: NonNullLiveData<Int> = satelliteInfoClient.satellitesUsedInLastFix

    override fun start(retainMockAccuracy: Boolean, accuracyThreshold: Float?) {
        if (accuracyThreshold != null) {
            this.accuracyThreshold = accuracyThreshold
        }

        locationTracker.start(retainMockAccuracy, 1000L)
    }

    override fun forceLocation() {
        acceptLocation(trackerLocation.value!!, true)
    }

    public override fun onCleared() {
        repeat.cancel()
        locationTracker.stop()
    }

    private fun updateLocation() {
        locationTracker.getCurrentLocation().let {
            trackerLocation.value = it

            if (it != null && it.accuracy <= accuracyThreshold) {
                acceptLocation(it, false)
            }
        }
    }

    private fun acceptLocation(location: Location, isManual: Boolean) {
        if (_acceptedLocation.value == null) {
            _acceptedLocation.value = location

            if (isManual) {
                logSavePointManual(location)
            } else {
                log(AnalyticsEvents.SAVE_POINT_AUTO)
            }
        }
    }

    private fun logSavePointManual(location: Location) {
        val event = if (System.currentTimeMillis() - startTime < 2000) {
            AnalyticsEvents.SAVE_POINT_IMMEDIATE
        } else {
            AnalyticsEvents.SAVE_POINT_MANUAL
        }

        if (location.accuracy > 100) {
            log(event, "accuracy", "unacceptable")
        } else if (location.accuracy > 10) {
            log(event, "accuracy", "poor")
        } else {
            log(event, "accuracy", "acceptable")
        }
    }
}

interface GeoPointViewModelFactory : ViewModelProvider.Factory
