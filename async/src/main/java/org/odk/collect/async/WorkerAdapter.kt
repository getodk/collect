package org.odk.collect.async

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

abstract class WorkerAdapter(private val spec: TaskSpec, context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val tag = if (tags.isNotEmpty()) {
            tags.first()
        } else {
            null
        }

        val completed = spec.getTask(applicationContext, tag).get()

        return if (completed) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
