package org.odk.collect.geo.support

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.odk.collect.location.Location
import org.odk.collect.location.tracker.LocationTracker

class FakeLocationTracker : LocationTracker {

    var currentLocation: Location? = null
        set(value) {
            if (isStarted) {
                _currentLocation.value = value
                field = value
            }
        }

    var retainMockAccuracy: Boolean = false
        private set

    private val _currentLocation: MutableStateFlow<Location?> = MutableStateFlow(currentLocation)

    var isStarted = false
        private set

    override fun getLocation(): StateFlow<Location?> {
        return _currentLocation
    }

    override fun start(
        retainMockAccuracy: Boolean,
        updateInterval: Long?,
        notification: Boolean
    ) {
        this.retainMockAccuracy = retainMockAccuracy

        isStarted = true
        _currentLocation.value = null
    }

    override fun stop() {
        isStarted = false
        _currentLocation.value = null
    }

    override fun bindToLifecycle(
        lifecycle: LifecycleOwner,
        retainMockAccuracy: Boolean
    ) {
        lifecycle.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                start(
                    retainMockAccuracy = retainMockAccuracy,
                    updateInterval = null,
                    notification = false
                )
            }

            override fun onPause(owner: LifecycleOwner) {
                stop()
            }
        })
    }
}
