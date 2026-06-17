package org.odk.collect.androidtest

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.odk.collect.async.DispatcherProvider

class TestDispatcherProvider : DispatcherProvider {
    private val testScope = TestScope()
    private val scheduler = testScope.testScheduler

    override val foreground = StandardTestDispatcher(scheduler = scheduler)
    override val background = StandardTestDispatcher(scheduler = scheduler)

    fun flush() {
        scheduler.advanceUntilIdle()
    }
}
