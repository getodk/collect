package org.odk.collect.async.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.odk.collect.async.Cancellable
import org.odk.collect.async.ScopeCancellable
import org.odk.collect.async.TaskRunner
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.coroutines.CoroutineContext

class CoroutineTaskRunner @JvmOverloads constructor(
    private val foregroundContext: CoroutineContext = Dispatchers.Main,
    private val backgroundContext: CoroutineContext = Dispatchers.IO
) : TaskRunner {
    override fun <T> immediate(
        background: Supplier<T>,
        foreground: Consumer<T>,
        delay: Long?,
        repeat: Long?
    ) {
        CoroutineScope(foregroundContext).launch {
            val result = withContext(backgroundContext) { background.get() }
            foreground.accept(result)
        }
    }

    override fun immediate(
        task: Runnable,
        isForeground: Boolean,
        delay: Long?,
        repeatPeriod: Long?
    ): Cancellable {
        val context = if (!isForeground) {
            backgroundContext
        } else {
            foregroundContext
        }

        val coroutineScope = CoroutineScope(context)
        coroutineScope.launch {
            if (delay != null) {
                delay(delay)
            }

            if (repeatPeriod != null) {
                while (isActive) {
                    task.run()
                    delay(repeatPeriod)
                }
            } else {
                task.run()
            }
        }

        return ScopeCancellable(coroutineScope)
    }

    override fun <T> flowOnBackground(flow: Flow<T>): Flow<T> {
        return flow.flowOn(backgroundContext)
    }
}
