package org.odk.collect.async

import kotlinx.coroutines.*
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.coroutines.CoroutineContext

class CoroutineScheduler(private val foreground: CoroutineContext, private val background: CoroutineContext) : Scheduler {

    constructor() : this(Dispatchers.Main, Dispatchers.IO) // Needed for Java construction

    override fun <T> scheduleInBackground(task: Supplier<T>, callback: Consumer<T>) {
        CoroutineScope(foreground).launch {
            callback.accept(withContext(background) { task.get() })
        }
    }

    override fun schedule(task: Runnable, period: Long): Cancellable {
        val repeatScope = CoroutineScope(foreground)

        repeatScope.launch {
            while (isActive) {
                task.run()
                delay(period)
            }
        }

        return Cancellable {
            repeatScope.cancel()
            true
        }
    }
}