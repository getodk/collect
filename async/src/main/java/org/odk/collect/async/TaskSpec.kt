package org.odk.collect.async

import android.content.Context
import java.util.function.Supplier

interface TaskSpec {

    /**
     * Should return the work to be carried out by the task. The return value of the work
     * indicates whether the work completed (true) or needs to be run again later (false)
     */
    fun getTask(context: Context, inputData: Map<String, String>): Supplier<Boolean>

    /**
     * Returns class that can be used to schedule this task using Android's
     * WorkManager framework
     */
    fun getWorkManagerAdapter(): Class<out WorkerAdapter>
}
