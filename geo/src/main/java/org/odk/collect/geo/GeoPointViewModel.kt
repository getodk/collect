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

    private val startTime = System.currentTimeMillis()
    private val repeat = scheduler.repeat(
        {
            _location.value = locationTracker.getCurrentLocation()
        },
        1000
    )

    init {
        locationTracker.start()
    }

    override var accuracyThreshold: Double = Double.MAX_VALUE

    private var forcedLocation: Location? = null
    private val _location = MutableLiveData<Location?>(null)
    override val location: LiveData<Location?>
        get() = Transformations.map(_location) {
            if (it != null) {
                if (forcedLocation != null) {
                    forcedLocation
                } else if (it.accuracy <= accuracyThreshold) {
                    it
                } else {
                    null
                }
            } else {
                null
            }
        }

    override val currentAccuracy: LiveData<Float?>
        get() = Transformations.map(_location) {
            it?.accuracy
        }

    override fun forceLocation() {
        forcedLocation = _location.value
        _location.value = _location.value

        logSavePointManual(forcedLocation!!)
    }

    public override fun onCleared() {
        repeat.cancel()
        locationTracker.stop()
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
