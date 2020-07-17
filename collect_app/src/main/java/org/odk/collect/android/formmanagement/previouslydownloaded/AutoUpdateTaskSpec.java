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

package org.odk.collect.android.formmanagement.previouslydownloaded;

import android.content.Context;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.android.notifications.DatabaseNotificationRepository;
import org.odk.collect.android.notifications.NotificationManagerNotifier;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.utilities.MultiFormDownloader;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import javax.inject.Inject;

public class AutoUpdateTaskSpec implements TaskSpec {

    @Inject
    ServerFormsDetailsFetcher serverFormsDetailsFetcher;

    @Inject
    StorageMigrationRepository storageMigrationRepository;

    @Inject
    NetworkStateProvider connectivityProvider;

    @Inject
    MultiFormDownloader multiFormDownloader;

    DatabaseNotificationRepository notificationRepository;

    @NotNull
    @Override
    public Runnable getTask(@NotNull Context context) {
        DaggerUtils.getComponent(context).inject(this);
        notificationRepository = new DatabaseNotificationRepository();

        return () -> {
            if (!connectivityProvider.isDeviceOnline() || storageMigrationRepository.isMigrationBeingPerformed()) {
                return;
            }

            new ServerFormsUpdateNotifier(
                    multiFormDownloader,
                    serverFormsDetailsFetcher,
                    new DatabaseNotificationRepository(),
                    new NotificationManagerNotifier(context)
            ).checkAndNotify();
        };
    }

    @NotNull
    @Override
    public Class<? extends WorkerAdapter> getWorkManagerAdapter() {
        return Adapter.class;
    }

    public static class Adapter extends WorkerAdapter {

        Adapter(@NotNull Context context, @NotNull WorkerParameters workerParams) {
            super(new AutoUpdateTaskSpec(), context, workerParams);
        }
    }

}