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

    override fun getTask(context: Context, inputData: Map<String, String>): Supplier<Boolean> {
        DaggerUtils.getComponent(context).inject(this)
        return Supplier {
            formsUpdater.matchFormsWithServer(
                inputData[DATA_PROJECT_ID]!!
            )
        }
    }

    override fun getWorkManagerAdapter(): Class<out WorkerAdapter> {
        return Adapter::class.java
    }

    class Adapter(context: Context, workerParams: WorkerParameters) :
        WorkerAdapter(SyncFormsTaskSpec(), context, workerParams)

    companion object {
        const val DATA_PROJECT_ID = "projectId"
    }
}
