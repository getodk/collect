package org.odk.collect.testshared

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.odk.collect.androidshared.utils.CoroutineDispatchersProvider

@ExperimentalCoroutinesApi
class TestCoroutineDispatchers : CoroutineDispatchersProvider {
    private val testDispatcher = UnconfinedTestDispatcher()

    override val main: CoroutineDispatcher
        get() = testDispatcher

    override val io: CoroutineDispatcher
        get() = testDispatcher

    override val default: CoroutineDispatcher
        get() = testDispatcher
}
