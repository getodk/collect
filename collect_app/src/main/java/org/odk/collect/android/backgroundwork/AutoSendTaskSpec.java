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
import android.util.Pair;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.forms.FormsRepository;
import org.odk.collect.android.gdrive.GoogleAccountsManager;
import org.odk.collect.android.gdrive.GoogleApiProvider;
import org.odk.collect.android.instancemanagement.InstanceSubmitter;
import org.odk.collect.android.instancemanagement.SubmitException;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;

import static java.lang.Boolean.parseBoolean;

public class AutoSendTaskSpec implements TaskSpec {

    @Inject
    NetworkStateProvider connectivityProvider;

    @Inject
    Analytics analytics;

    @Inject
    Notifier notifier;

    @Inject
    @Named("INSTANCES")
    ChangeLock changeLock;

    @Inject
    FormsRepository formsRepository;

    @Inject
    InstancesRepository instancesRepository;

    @Inject
    GoogleAccountsManager googleAccountsManager;

    @Inject
    GoogleApiProvider googleApiProvider;

    @Inject
    PermissionsProvider permissionsProvider;

    @Inject
    SettingsProvider settingsProvider;

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
    public Supplier<Boolean> getTask(@NotNull Context context) {
        return () -> {
            Collect.getInstance().getComponent().inject(this);

            NetworkInfo currentNetworkInfo = connectivityProvider.getNetworkInfo();
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !(networkTypeMatchesAutoSendSetting(currentNetworkInfo) || atLeastOneFormSpecifiesAutoSend())) {
                if (!networkTypeMatchesAutoSendSetting(currentNetworkInfo)) {
                    return false;
                }

                return true;
            }

            return changeLock.withLock(acquiredLock -> {
                if (acquiredLock) {
                    try {
                        Pair<Boolean, String> results = new InstanceSubmitter(analytics, formsRepository, instancesRepository, googleAccountsManager, googleApiProvider, permissionsProvider, settingsProvider).submitUnsubmittedInstances();
                        notifier.onSubmission(results.first, results.second);
                    } catch (SubmitException e) {
                        switch (e.getType()) {
                            case GOOGLE_ACCOUNT_NOT_SET:
                                notifier.onSubmission(true, context.getString(R.string.google_set_account));
                                break;
                            case GOOGLE_ACCOUNT_NOT_PERMITTED:
                                notifier.onSubmission(true, context.getString(R.string.odk_permissions_fail));
                                break;
                            case NOTHING_TO_SUBMIT:
                                break;
                        }
                    }

                    return true;
                } else {
                    return false;
                }
            });
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
    private boolean networkTypeMatchesAutoSendSetting(NetworkInfo currentNetworkInfo) {
        if (currentNetworkInfo == null) {
            return false;
        }

        String autosend = settingsProvider.getGeneralSettings().getString(GeneralKeys.KEY_AUTOSEND);
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
    private boolean atLeastOneFormSpecifiesAutoSend() {
        return formsRepository.getAll().stream().anyMatch(form -> parseBoolean(form.getAutoSend()));
    }

    public static class Adapter extends WorkerAdapter {

        public Adapter(@NotNull Context context, @NotNull WorkerParameters workerParams) {
            super(new AutoSendTaskSpec(), context, workerParams);
        }
    }
}
