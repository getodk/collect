package org.odk.collect.geo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.location.Location
import org.odk.collect.location.tracker.LocationTracker

internal abstract class GeoPointViewModel : ViewModel() {
    abstract var accuracyThreshold: Double
    abstract val location: Location?
    abstract val currentAccuracy: Float?
}

internal class GeoPointViewModelImpl(
    private val locationTracker: LocationTracker
) : GeoPointViewModel() {

    init {
        locationTracker.start()
    }

    override var accuracyThreshold: Double = Double.MAX_VALUE

    override val location: Location?
        get() {
            return locationTracker.getCurrentLocation()?.let {
                if (it.accuracy <= accuracyThreshold) {
                    it
                } else {
                    null
                }
            }
        }

    override val currentAccuracy: Float?
        get() = locationTracker.getCurrentLocation()?.accuracy

    public override fun onCleared() {
        locationTracker.stop()
    }
}

interface GeoPointViewModelFactory : ViewModelProvider.Factory
