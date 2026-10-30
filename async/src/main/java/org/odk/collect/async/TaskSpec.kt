package org.odk.collect.async

import android.content.Context
import androidx.work.BackoffPolicy
import java.util.function.Supplier

interface TaskSpec {
    val maxRetries: Int?
    val backoffPolicy: BackoffPolicy?
    val backoffDelay: Long?

    /**
     * Should return the work to be carried out by the task. The output data in the returned
     * [Result] will be passed to chained tasks.
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
    ): Supplier<Result>

    /**
     * Called if an exception is thrown while executing the work.
     */
    fun onException(exception: Throwable)

    sealed class Result {
        abstract val outputData: Map<String, String>

        data class Success(override val outputData: Map<String, String> = emptyMap()) : Result()
        data class Failure(override val outputData: Map<String, String> = emptyMap()) : Result()
    }
}

fun TaskSpec.run(
    context: Context,
    inputData: Map<String, String>,
    runAttemptCount: Int,
    isForeground: Boolean,
    isStopped: () -> Boolean
): RunResult {
    try {
        val isLastUniqueExecution = maxRetries?.let { runAttemptCount >= it } ?: true
        val result =
            getTask(context, inputData, isForeground || isLastUniqueExecution, isStopped).get()
        val maxRetries = this.maxRetries

        return when (result) {
            is TaskSpec.Result.Success -> {
                RunResult.Success(result.outputData)
            }

            is TaskSpec.Result.Failure -> {
                if (!isForeground && (maxRetries == null || runAttemptCount < maxRetries)) {
                    RunResult.Retry(result.outputData)
                } else {
                    RunResult.Failure(result.outputData)
                }
            }
        }
    } catch (t: Throwable) {
        onException(t)
        return RunResult.Error
    }
}

sealed class RunResult {

    abstract val outputData: Map<String, String>?

    data class Success(override val outputData: Map<String, String>) : RunResult()
    data class Retry(override val outputData: Map<String, String>) : RunResult()
    data class Failure(override val outputData: Map<String, String>) : RunResult()
    data object Error : RunResult() {
        override val outputData: Map<String, String>? = null
    }
}
