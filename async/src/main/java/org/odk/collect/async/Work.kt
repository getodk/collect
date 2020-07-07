package org.odk.collect.async

import android.content.Context

interface Work {

    fun doWork(context: Context)

    /**
     * Returns class that can be used to schedule this Work implementation using Android's
     * WorkManager framework
     */
    fun <T : Work> getWorkerClass(): Class<out WorkerAdapter<T>>
}