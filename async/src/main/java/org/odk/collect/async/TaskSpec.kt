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
    fun getTask(
        context: Context,
        inputData: Map<String, String>,
        isLastUniqueExecution: Boolean,
        isStopped: (() -> Boolean)
    ): Supplier<Boolean>

    /**
     * Called if an exception is thrown while executing the work.
     */
    fun onException(exception: Throwable)

    enum class Result {
        SUCCESS,
        FAILURE,
        RETRY
    }
}

fun TaskSpec.run(
    context: Context,
    inputData: Map<String, String>,
    runAttemptCount: Int,
    isForeground: Boolean,
    isStopped: () -> Boolean
): TaskSpec.Result {
    try {
        val isLastUniqueExecution = maxRetries?.let { runAttemptCount >= it } ?: true
        val completed = getTask(context, inputData, isLastUniqueExecution, isStopped).get()
        val maxRetries = this.maxRetries

        return if (completed) {
            TaskSpec.Result.SUCCESS
        } else if (maxRetries == null || runAttemptCount < maxRetries) {
            TaskSpec.Result.RETRY
        } else {
            TaskSpec.Result.FAILURE
        }
    } catch (t: Throwable) {
        onException(t)
        return TaskSpec.Result.FAILURE
    }
}
