package org.odk.collect.async

import android.content.Context
import java.util.function.Supplier

interface TaskSpec {
    val maxRetries: Int

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

    companion object {
        /**
         * If a task specifies maxRetries all retries form one logical group. We want to know
         * which task execution is the last one to for example notify a user only once instead of
         * doing that after every single execution.
         */
        const val DATA_LAST_UNIQUE_EXECUTION = "lastUniqueExecution"
        const val UNLIMITED_NUMBER_OF_RETRIES = -1
    }
}
