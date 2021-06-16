package org.odk.collect.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.coroutines.CoroutineContext

open class CoroutineScheduler(private val foregroundContext: CoroutineContext, private val backgroundContext: CoroutineContext) : Scheduler {

    override fun <T> immediate(background: Supplier<T>, foreground: Consumer<T>) {
        CoroutineScope(foregroundContext).launch {
            val result = withContext(backgroundContext) { background.get() }
            foreground.accept(result)
        }
    }

    override fun immediate(foreground: Runnable) {
        CoroutineScope(foregroundContext).launch {
            foreground.run()
        }
    }

    override fun repeat(foreground: Runnable, repeatPeriod: Long): Cancellable {
        val repeatScope = CoroutineScope(foregroundContext)

        repeatScope.launch {
            while (isActive) {
                foreground.run()
                delay(repeatPeriod)
            }
        }

        return ScopeCancellable(repeatScope)
    }

    override fun cancelAllDeferred() {
        throw UnsupportedOperationException()
    }

    override fun networkDeferred(tag: String, spec: TaskSpec, inputData: Map<String, String>) {
        throw UnsupportedOperationException()
    }

    override fun networkDeferred(tag: String, spec: TaskSpec, repeatPeriod: Long, inputData: Map<String, String>) {
        throw UnsupportedOperationException()
    }

    override fun cancelDeferred(tag: String) {
        throw UnsupportedOperationException()
    }

    override fun isDeferredRunning(tag: String): Boolean {
        throw UnsupportedOperationException()
    }
}
