package org.odk.collect.async

import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.Flow
import java.util.function.Consumer
import java.util.function.Supplier

interface TaskRunner {
    fun <T> immediate(background: Supplier<T>, foreground: Consumer<T>, delay: Long? = null, repeat: Long? = null)
    fun immediate(task: Runnable, isForeground: Boolean = false, delay: Long? = null, repeatPeriod: Long? = null): Cancellable
    fun <T> flowOnBackground(flow: Flow<T>): Flow<T>
}
