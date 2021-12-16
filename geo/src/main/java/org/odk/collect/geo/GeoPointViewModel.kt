package org.odk.collect.geo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.async.Scheduler
import org.odk.collect.location.Location
import org.odk.collect.location.tracker.LocationTracker

internal abstract class GeoPointViewModel : ViewModel() {
    abstract var accuracyThreshold: Double
    abstract val location: LiveData<Location?>
    abstract val currency: LiveData<Float?>
}

internal class GeoPointViewModelImpl(
    private val locationTracker: LocationTracker,
    private val scheduler: Scheduler
) : GeoPointViewModel() {

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

    private val _location = MutableLiveData<Location?>(null)
    override val location: LiveData<Location?>
        get() = Transformations.map(_location) {
            if (it != null && it.accuracy <= accuracyThreshold) {
                it
            } else {
                null
            }
        }

    override val currency: LiveData<Float?>
        get() = Transformations.map(_location) {
            it?.accuracy
        }

    public override fun onCleared() {
        repeat.cancel()
        locationTracker.stop()
    }
}

interface GeoPointViewModelFactory : ViewModelProvider.Factory
