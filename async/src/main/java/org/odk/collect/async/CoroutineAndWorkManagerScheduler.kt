package org.odk.collect.async

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.coroutines.CoroutineContext

class CoroutineAndWorkManagerScheduler(private val foregroundContext: CoroutineContext, private val backgroundContext: CoroutineContext, private val workManager: WorkManager) : Scheduler {

    constructor(workManager: WorkManager) : this(Dispatchers.Main, Dispatchers.IO, workManager) // Needed for Java construction

    override fun <T> immediate(background: Supplier<T>, foreground: Consumer<T>) {
        CoroutineScope(foregroundContext).launch {
            val result = withContext(backgroundContext) { background.get() }
            foreground.accept(result)
        }
    }

    override fun immediate(foreground: java.lang.Runnable) {
        CoroutineScope(foregroundContext).launch {
            foreground.run()
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
        val repeatScope = CoroutineScope(foregroundContext)

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
