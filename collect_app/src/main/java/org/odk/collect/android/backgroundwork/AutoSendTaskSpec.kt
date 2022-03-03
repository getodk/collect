/*
 * Copyright (C) 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.backgroundwork

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.WorkerParameters
import org.odk.collect.android.backgroundwork.autosend.FormLevelAutoSendChecker
import org.odk.collect.android.backgroundwork.autosend.GeneralAutoSendChecker
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.instancemanagement.InstanceAutoSender
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.WorkerAdapter
import org.odk.collect.settings.SettingsProvider
import java.util.function.Supplier
import javax.inject.Inject

class AutoSendTaskSpec : TaskSpec {
    @Inject
    lateinit var connectivityProvider: NetworkStateProvider

    @Inject
    lateinit var formsRepositoryProvider: FormsRepositoryProvider

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var instanceAutoSender: InstanceAutoSender

    @Inject
    lateinit var generalAutoSendChecker: GeneralAutoSendChecker

    @Inject
    lateinit var formLevelAutoSendChecker: FormLevelAutoSendChecker

    override val maxRetries: Int? = null
    override val backoffPolicy: BackoffPolicy? = null
    override val backoffDelay: Long? = null

    /**
     * If the app-level auto-send setting is enabled, send all finalized forms that don't specify not
     * to auto-send at the form level. If the app-level auto-send setting is disabled, send all
     * finalized forms that specify to send at the form level.
     *
     *
     * Fails immediately if:
     * - storage isn't ready
     * - the network type that toggled on is not the desired type AND no form specifies auto-send
     *
     *
     * If the network type doesn't match the auto-send settings, retry next time a connection is
     * available.
     */
    override fun getTask(context: Context, inputData: Map<String, String>, isLastUniqueExecution: Boolean): Supplier<Boolean> {
        DaggerUtils.getComponent(context).inject(this)
        return Supplier {
            val projectId = inputData[TaskData.DATA_PROJECT_ID]
            if (projectId != null) {
                if (generalAutoSendChecker.isAutoSendEnabled(projectId) || formLevelAutoSendChecker.isAutoSendEnabled(projectId)) {
                    return@Supplier instanceAutoSender.autoSendInstances(projectId)
                }
                false
            } else {
                throw IllegalArgumentException("No project ID provided!")
            }
        }
    }

    override fun getWorkManagerAdapter(): Class<out WorkerAdapter> {
        return Adapter::class.java
    }

    class Adapter(context: Context, workerParams: WorkerParameters) :
        WorkerAdapter(AutoSendTaskSpec(), context, workerParams)
}
