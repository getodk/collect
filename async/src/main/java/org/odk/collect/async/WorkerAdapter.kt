package org.odk.collect.async

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

abstract class WorkerAdapter<T : Work>(private val workClass: Class<T>, context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        workClass.newInstance().doWork(applicationContext)
        return Result.success()
    }
}