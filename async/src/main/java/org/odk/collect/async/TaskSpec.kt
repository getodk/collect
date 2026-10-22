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
    ): Supplier<Map<String, String>?>

    /**
     * Called if an exception is thrown while executing the work.
     */
    fun onException(exception: Throwable)

    sealed class Result {
        data class SUCCESS(val outputData: Map<String, String>) : Result()
        data object FAILURE : Result()
        data object RETRY : Result()
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
        val outputData =
            getTask(context, inputData, isForeground || isLastUniqueExecution, isStopped).get()
        val maxRetries = this.maxRetries

        return if (outputData != null) {
            TaskSpec.Result.SUCCESS(outputData)
        } else if (!isForeground && (maxRetries == null || runAttemptCount < maxRetries)) {
            TaskSpec.Result.RETRY
        } else {
            TaskSpec.Result.FAILURE
        }
    } catch (t: Throwable) {
        onException(t)
        return TaskSpec.Result.FAILURE
    }
}
