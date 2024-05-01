package org.odk.collect.android.backgroundwork

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.WorkerParameters
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.WorkerAdapter
import java.util.function.Supplier
import javax.inject.Inject

class SendFormTaskSpec : TaskSpec {

    @Inject
    lateinit var instancesDataService: InstancesDataService

    override val maxRetries: Int? = null
    override val backoffPolicy = BackoffPolicy.EXPONENTIAL
    override val backoffDelay: Long = 60_000

    override fun getTask(
        context: Context,
        inputData: Map<String, String>,
        isLastUniqueExecution: Boolean
    ): Supplier<Boolean> {
        DaggerUtils.getComponent(context).inject(this)

        return Supplier {
            val projectId = inputData[TaskData.DATA_PROJECT_ID]!!
            val instanceId = inputData[TaskData.DATA_INSTANCE_ID]!!.toLong()
            instancesDataService.sendInstances(projectId, listOf(instanceId))
            true
        }
    }

    override fun getWorkManagerAdapter(): Class<out WorkerAdapter> {
        return Adapter::class.java
    }

    class Adapter(context: Context, workerParams: WorkerParameters) :
        WorkerAdapter(SendFormTaskSpec(), context, workerParams)
}
