/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.backgroundwork

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.WorkerParameters
import org.odk.collect.android.formmanagement.FormsUpdater
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.WorkerAdapter
import java.util.function.Supplier
import javax.inject.Inject

class AutoUpdateTaskSpec : TaskSpec {
    @Inject
    lateinit var formsUpdater: FormsUpdater

    override val maxRetries: Int? = null
    override val backoffPolicy: BackoffPolicy? = null
    override val backoffDelay: Long? = null

    override fun getTask(context: Context, inputData: Map<String, String>, isLastUniqueExecution: Boolean): Supplier<Boolean> {
        DaggerUtils.getComponent(context).inject(this)
        return Supplier {
            val projectId = inputData[TaskData.DATA_PROJECT_ID]
            if (projectId != null) {
                formsUpdater.downloadUpdates(projectId)
                true
            } else {
                throw IllegalArgumentException("No project ID provided!")
            }
        }
    }

    override fun getWorkManagerAdapter(): Class<out WorkerAdapter> {
        return Adapter::class.java
    }

    class Adapter(context: Context, workerParams: WorkerParameters) :
        WorkerAdapter(AutoUpdateTaskSpec(), context, workerParams)
}
