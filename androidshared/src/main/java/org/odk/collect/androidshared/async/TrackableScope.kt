package org.odk.collect.androidshared.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class TrackableScope(private val coroutineScope: CoroutineScope) {

    private val _isWorking = MutableStateFlow(false)
    val isWorking: StateFlow<Boolean> = _isWorking

    private var activeBackgroundJobsCounter = AtomicInteger(0)

    fun launch(block: suspend CoroutineScope.() -> Unit) {
        activeBackgroundJobsCounter.incrementAndGet()
        _isWorking.value = true

        coroutineScope.launch {
            block(this)

            if (activeBackgroundJobsCounter.decrementAndGet() == 0) {
                _isWorking.value = false
            }
        }
    }
}
