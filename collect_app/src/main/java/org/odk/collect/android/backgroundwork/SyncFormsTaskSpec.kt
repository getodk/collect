package org.odk.collect.android.backgroundwork

import android.content.Context
import androidx.work.BackoffPolicy
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.async.TaskSpec
import java.util.function.Supplier
import javax.inject.Inject

class SyncFormsTaskSpec : TaskSpec {
    @Inject
    lateinit var formsDataService: FormsDataService

    @Inject
    lateinit var notifier: Notifier

    override val maxRetries = 3
    override val backoffPolicy = BackoffPolicy.EXPONENTIAL
    override val backoffDelay: Long = 60_000

    override fun getTask(
        context: Context,
        inputData: Map<String, String>,
        isLastUniqueExecution: Boolean,
        isStopped: (() -> Boolean)
    ): Supplier<Boolean> {
        DaggerUtils.getComponent(context).inject(this)

        return Supplier {
            val projectId = inputData[TaskData.DATA_PROJECT_ID]
            if (projectId != null) {
                formsDataService.matchFormsWithServer(projectId, isLastUniqueExecution, isStopped)
            } else {
                throw IllegalArgumentException("No project ID provided!")
            }
        }
    }

    override fun onException(exception: Throwable) {
        Analytics.logNonFatal(exception)
    }
}
