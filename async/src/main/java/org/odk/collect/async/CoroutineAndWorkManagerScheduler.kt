package org.odk.collect.async

import androidx.work.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.coroutines.CoroutineContext

class CoroutineAndWorkManagerScheduler(private val foreground: CoroutineContext, private val background: CoroutineContext, private val workManager: WorkManager) : Scheduler {

    constructor(workManager: WorkManager) : this(Dispatchers.Main, Dispatchers.IO, workManager) // Needed for Java construction

    override fun <T> immediate(background: Supplier<T>, foreground: Consumer<T>) {
        CoroutineScope(this.foreground).launch {
            foreground.accept(withContext(this@CoroutineAndWorkManagerScheduler.background) { background.get() })
        }
    }

    override fun networkDeferred(tag: String, spec: TaskSpec) {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val workRequest = OneTimeWorkRequest.Builder(spec.getWorkManagerAdapter())
                .addTag(tag)
                .setConstraints(constraints)
                .build()

        workManager.beginUniqueWork(tag, ExistingWorkPolicy.KEEP, workRequest).enqueue()
    }

    override fun networkDeferred(tag: String, spec: TaskSpec, repeatPeriod: Long) {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val worker = spec.getWorkManagerAdapter()
        val workRequest = PeriodicWorkRequest.Builder(worker, repeatPeriod, TimeUnit.MILLISECONDS)
                .addTag(tag)
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
    }

    override fun repeat(foreground: Runnable, repeatPeriod: Long): Cancellable {
        val repeatScope = CoroutineScope(this.foreground)

        repeatScope.launch {
            while (isActive) {
                foreground.run()
                delay(repeatPeriod)
            }
        }

        return ScopeCancellable(repeatScope)
    }

    override fun cancelDeferred(tag: String) {
        workManager.cancelUniqueWork(tag)
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

