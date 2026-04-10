package org.odk.collect.androidtest

import kotlinx.coroutines.test.StandardTestDispatcher
import org.odk.collect.async.DispatcherProvider

class TestDispatcherProvider : DispatcherProvider {
    override val foreground = StandardTestDispatcher()
    override val background = StandardTestDispatcher()

    fun runBackground() {
        background.scheduler.advanceUntilIdle()
    }
}
