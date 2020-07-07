package org.odk.collect.async

import android.content.Context

interface TaskSpec {

    fun getTask(context: Context): Runnable

    /**
     * Returns class that can be used to schedule this task using Android's
     * WorkManager framework
     */
    fun getWorkManagerAdapter(): Class<out WorkerAdapter>
}