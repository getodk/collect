package org.odk.collect.async

import android.content.Context
import androidx.work.BackoffPolicy
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.mock
import org.odk.collect.async.TaskSpec.Result
import java.util.function.Supplier

class TaskSpecTest {

    @Test
    fun `#run doesn't return retry if isForeground is true`() {
        val result = OneRetryTaskSpec().run(mock(), emptyMap(), 1, true, { false })
        assertThat(result, equalTo(RunResult.Failure))
    }
}

private class OneRetryTaskSpec() : TaskSpec {
    override val maxRetries: Int = 2
    override val backoffPolicy: BackoffPolicy? = null
    override val backoffDelay: Long? = null

    override fun getTask(
        context: Context,
        inputData: Map<String, String>,
        isLastUniqueExecution: Boolean,
        isStopped: () -> Boolean
    ): Supplier<Result> {
        return Supplier {
            Result.Failure()
        }
    }

    override fun onException(exception: Throwable) {}
}
