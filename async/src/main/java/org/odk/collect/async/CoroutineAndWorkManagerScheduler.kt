package org.odk.collect.async

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.coroutines.CoroutineContext

class CoroutineAndWorkManagerScheduler(private val foreground: CoroutineContext, private val background: CoroutineContext, private val workManager: WorkManager) : Scheduler {

    constructor(workManager: WorkManager) : this(Dispatchers.Main, Dispatchers.IO, workManager) // Needed for Java construction

    override fun <T> runInBackground(task: Supplier<T>, callback: Consumer<T>) {
        CoroutineScope(foreground).launch {
            callback.accept(withContext(background) { task.get() })
        }
    }

    override fun schedule(task: Runnable, repeatPeriod: Long): Cancellable {
        val repeatScope = CoroutineScope(foreground)

        repeatScope.launch {
            while (isActive) {
                task.run()
                delay(repeatPeriod)
            }
        }

        return ScopeCancellable(repeatScope)
    }

    override fun scheduleInBackground(tag: String, spec: TaskSpec, repeatPeriod: Long) {
        val workRequest = PeriodicWorkRequest.Builder(
                spec.getWorkManagerAdapter(),
                repeatPeriod,
                TimeUnit.MILLISECONDS
        ).build()

        workManager.enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
    }


    override fun isRunning(tag: String): Boolean {
        return isWorkManagerWorkRunning(tag)
    }

    private fun isWorkManagerWorkRunning(tag: String): Boolean {
        val statuses = workManager.getWorkInfosByTag(tag)
        for (workInfo in statuses.get()) {
            if (workInfo.state == WorkInfo.State.RUNNING) {
                return true
            }
        }

        return false
    }
}

private class ScopeCancellable(private val scope: CoroutineScope) : Cancellable {

    override fun cancel(): Boolean {
        scope.cancel()
        return true
    }
}

