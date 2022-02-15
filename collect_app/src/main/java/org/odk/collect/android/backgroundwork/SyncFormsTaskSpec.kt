package org.odk.collect.android.backgroundwork

import android.content.Context
import androidx.work.WorkerParameters
import org.odk.collect.android.formmanagement.FormsUpdater
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.WorkerAdapter
import java.util.function.Supplier
import javax.inject.Inject

class SyncFormsTaskSpec : TaskSpec {
    @Inject
    lateinit var formsUpdater: FormsUpdater

    override val maxRetries = 3

    override fun getTask(context: Context, inputData: Map<String, String>): Supplier<Boolean> {
        DaggerUtils.getComponent(context).inject(this)
        return Supplier {
            val projectId = inputData[TaskSpec.DATA_PROJECT_ID]
            val notify = inputData[TaskSpec.DATA_LAST_UNIQUE_EXECUTION].toBoolean()
            if (projectId != null) {
                return@Supplier formsUpdater.matchFormsWithServer(projectId, notify)
            } else {
                throw IllegalArgumentException("No project ID provided!")
            }
        }
    }

    override fun getWorkManagerAdapter(): Class<out WorkerAdapter> {
        return Adapter::class.java
    }

    class Adapter(context: Context, workerParams: WorkerParameters) :
        WorkerAdapter(SyncFormsTaskSpec(), context, workerParams)
}
