package org.odk.collect.async

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

abstract class WorkerAdapter(private val spec: TaskSpec, context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        spec.getTask(applicationContext).run()
        return Result.success()
    }
}