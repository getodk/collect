package org.odk.collect.geo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.analytics.Analytics.Companion.log
import org.odk.collect.async.Scheduler
import org.odk.collect.geo.analytics.AnalyticsEvents
import org.odk.collect.location.Location
import org.odk.collect.location.tracker.LocationTracker

internal abstract class GeoPointViewModel : ViewModel() {
    abstract var accuracyThreshold: Double
    abstract val location: LiveData<Location?>
    abstract val currentAccuracy: LiveData<Float?>

    abstract fun forceLocation()
}

internal class GeoPointViewModelImpl(
    private val locationTracker: LocationTracker,
    scheduler: Scheduler
) : GeoPointViewModel() {

    init {
        locationTracker.start()
    }

    private val startTime = System.currentTimeMillis()
    private val repeat = scheduler.repeat(
        {
            locationTracker.getCurrentLocation().let {
                trackerLocation.value = it

                if (it != null && it.accuracy <= accuracyThreshold) {
                    acceptLocation(it, false)
                }
            }
        },
        1000
    )

    private val trackerLocation = MutableLiveData<Location?>(null)
    private val acceptedLocation = MutableLiveData<Location?>(null)

    override var accuracyThreshold: Double = Double.MAX_VALUE
    override val location = acceptedLocation
    override val currentAccuracy = Transformations.map(trackerLocation) {
        it?.accuracy
    }

    override fun forceLocation() {
        acceptLocation(trackerLocation.value!!, true)
    }

    public override fun onCleared() {
        repeat.cancel()
        locationTracker.stop()
    }

    private fun acceptLocation(location: Location, isManual: Boolean) {
        if (acceptedLocation.value == null) {
            acceptedLocation.value = location

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
