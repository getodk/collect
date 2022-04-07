package org.odk.collect.testshared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import org.odk.collect.async.TaskSpec
import java.util.function.Consumer
import java.util.function.Supplier

@ExperimentalCoroutinesApi
class TestScheduler : Scheduler {
    private val testDispatcher = UnconfinedTestDispatcher()

    override fun <T> immediate(background: Supplier<T>, foreground: Consumer<T>) {
        CoroutineScope(testDispatcher).launch {
            val result = background.get()
            foreground.accept(result)
        }
    }

    override fun immediate(foreground: Runnable) {
        TODO("Not yet implemented")
    }

    override fun networkDeferred(tag: String, spec: TaskSpec, inputData: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun networkDeferred(
        tag: String,
        spec: TaskSpec,
        repeatPeriod: Long,
        inputData: Map<String, String>
    ) {
        TODO("Not yet implemented")
    }

    override fun cancelDeferred(tag: String) {
        TODO("Not yet implemented")
    }

    override fun isDeferredRunning(tag: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun repeat(foreground: Runnable, repeatPeriod: Long): Cancellable {
        TODO("Not yet implemented")
    }

    override fun cancelAllDeferred() {
        TODO("Not yet implemented")
    }
}
