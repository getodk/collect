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
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Environment
import androidx.work.BackoffPolicy
import androidx.work.WorkerParameters
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.instancemanagement.InstanceAutoSender
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.WorkerAdapter
import org.odk.collect.forms.Form
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
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
                val currentNetworkInfo = connectivityProvider.networkInfo
                if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED ||
                    !(networkTypeMatchesAutoSendSetting(currentNetworkInfo, projectId) || atLeastOneFormSpecifiesAutoSend(projectId))
                ) {
                    networkTypeMatchesAutoSendSetting(currentNetworkInfo, projectId)
                }
                instanceAutoSender.autoSendInstances(projectId)
            } else {
                throw IllegalArgumentException("No project ID provided!")
            }
        }
    }

    override fun getWorkManagerAdapter(): Class<out WorkerAdapter> {
        return Adapter::class.java
    }

    /**
     * Returns whether the currently-available connection type is included in the app-level auto-send
     * settings.
     *
     * @return true if a connection is available and settings specify it should trigger auto-send,
     * false otherwise.
     */
    private fun networkTypeMatchesAutoSendSetting(
        currentNetworkInfo: NetworkInfo?,
        projectId: String?
    ): Boolean {
        if (currentNetworkInfo == null) {
            return false
        }
        val autosend = settingsProvider.getUnprotectedSettings(projectId).getString(ProjectKeys.KEY_AUTOSEND)
        var sendwifi = autosend == "wifi_only"
        var sendnetwork = autosend == "cellular_only"

        if (autosend == "wifi_and_cellular") {
            sendwifi = true
            sendnetwork = true
        }
        return currentNetworkInfo.type == ConnectivityManager.TYPE_WIFI &&
            sendwifi || currentNetworkInfo.type == ConnectivityManager.TYPE_MOBILE && sendnetwork
    }

    /**
     * Returns true if at least one form currently on the device specifies that all of its filled
     * forms should auto-send no matter the connection type.
     */
    private fun atLeastOneFormSpecifiesAutoSend(projectId: String?): Boolean {
        return formsRepositoryProvider.get(projectId).all.stream()
            .anyMatch { form: Form -> java.lang.Boolean.parseBoolean(form.autoSend) }
    }

    class Adapter(context: Context, workerParams: WorkerParameters) :
        WorkerAdapter(AutoSendTaskSpec(), context, workerParams)
}
