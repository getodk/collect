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

package org.odk.collect.android.backgroundwork;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.instancemanagement.InstanceAutoSender;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;

import static java.lang.Boolean.parseBoolean;

public class AutoSendTaskSpec implements TaskSpec {

    public static final String DATA_PROJECT_ID = "projectId";

    @Inject
    NetworkStateProvider connectivityProvider;

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    InstanceAutoSender instanceAutoSender;

    /**
     * If the app-level auto-send setting is enabled, send all finalized forms that don't specify not
     * to auto-send at the form level. If the app-level auto-send setting is disabled, send all
     * finalized forms that specify to send at the form level.
     * <p>
     * Fails immediately if:
     * - storage isn't ready
     * - the network type that toggled on is not the desired type AND no form specifies auto-send
     * <p>
     * If the network type doesn't match the auto-send settings, retry next time a connection is
     * available.
     */
    @NotNull
    @Override
    public Supplier<Boolean> getTask(@NotNull Context context, @NotNull Map<String, String> inputData) {
        return () -> {
            DaggerUtils.getComponent(context).inject(this);

            String projectId = inputData.get(DATA_PROJECT_ID);
            NetworkInfo currentNetworkInfo = connectivityProvider.getNetworkInfo();
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !(networkTypeMatchesAutoSendSetting(currentNetworkInfo, projectId) || atLeastOneFormSpecifiesAutoSend(projectId))) {
                if (!networkTypeMatchesAutoSendSetting(currentNetworkInfo, projectId)) {
                    return false;
                }

                return true;
            }

            return instanceAutoSender.autoSendInstances(projectId);
        };
    }

    @NotNull
    @Override
    public Class<? extends WorkerAdapter> getWorkManagerAdapter() {
        return Adapter.class;
    }

    /**
     * Returns whether the currently-available connection type is included in the app-level auto-send
     * settings.
     *
     * @return true if a connection is available and settings specify it should trigger auto-send,
     * false otherwise.
     */
    private boolean networkTypeMatchesAutoSendSetting(NetworkInfo currentNetworkInfo, String projectId) {
        if (currentNetworkInfo == null) {
            return false;
        }

        String autosend = settingsProvider.getUnprotectedSettings(projectId).getString(ProjectKeys.KEY_AUTOSEND);
        boolean sendwifi = autosend.equals("wifi_only");
        boolean sendnetwork = autosend.equals("cellular_only");
        if (autosend.equals("wifi_and_cellular")) {
            sendwifi = true;
            sendnetwork = true;
        }

        return currentNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI
                && sendwifi || currentNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                && sendnetwork;
    }

    /**
     * Returns true if at least one form currently on the device specifies that all of its filled
     * forms should auto-send no matter the connection type.
     */
    private boolean atLeastOneFormSpecifiesAutoSend(String projectId) {
        return formsRepositoryProvider.get(projectId).getAll().stream().anyMatch(form -> parseBoolean(form.getAutoSend()));
    }

    public static class Adapter extends WorkerAdapter {

        public Adapter(@NotNull Context context, @NotNull WorkerParameters workerParams) {
            super(new AutoSendTaskSpec(), context, workerParams);
        }
    }
}
