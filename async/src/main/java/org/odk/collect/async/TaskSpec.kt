package org.odk.collect.async

import android.content.Context
import androidx.work.BackoffPolicy
import java.util.function.Supplier

interface TaskSpec {
    val maxRetries: Int?
    val backoffPolicy: BackoffPolicy?
    val backoffDelay: Long?

    /**
     * Should return the work to be carried out by the task. The return value of the work
     * indicates whether the work completed (true) or needs to be run again later (false)
     *
     * @param isLastUniqueExecution if a task specifies maxRetries all retries form one logical
     * group. We want to know which task execution is the last one to for example notify a user only
     * once instead of doing that after every single execution.
     */
    fun getTask(context: Context, inputData: Map<String, String>, isLastUniqueExecution: Boolean): Supplier<Boolean>

    /**
     * Returns class that can be used to schedule this task using Android's
     * WorkManager framework
     */
    fun getWorkManagerAdapter(): Class<out WorkerAdapter>
}
