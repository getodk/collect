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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormDownloadListActivity;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.android.notifications.NotificationRepository;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.utilities.MultiFormDownloader;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import static org.odk.collect.android.activities.FormDownloadListActivity.DISPLAY_ONLY_UPDATED_FORMS;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORMS_DOWNLOADED_NOTIFICATION;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORM_UPDATES_AVAILABLE_NOTIFICATION;
import static org.odk.collect.android.utilities.NotificationUtils.FORM_UPDATE_NOTIFICATION_ID;
import static org.odk.collect.android.utilities.NotificationUtils.showNotification;

public class AutoUpdateTaskSpec implements TaskSpec {

    @Inject
    ServerFormsDetailsFetcher serverFormsDetailsFetcher;

    @Inject
    StorageMigrationRepository storageMigrationRepository;

    @Inject
    NetworkStateProvider connectivityProvider;

    @Inject
    MultiFormDownloader multiFormDownloader;

    @Inject
    NotificationManager notificationManager;

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

            ServerFormsUpdateNotifier.Notifier notifier = new ServerFormsUpdateNotifier.Notifier() {

                @Override
                public void onUpdatesAvailable() {
                    Intent intent = new Intent(context, FormDownloadListActivity.class);
                    intent.putExtra(DISPLAY_ONLY_UPDATED_FORMS, true);
                    PendingIntent contentIntent = PendingIntent.getActivity(context, FORM_UPDATES_AVAILABLE_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    showNotification(
                            context,
                            notificationManager,
                            R.string.form_updates_available,
                            null,
                            contentIntent,
                            FORM_UPDATE_NOTIFICATION_ID
                    );
                }

                @Override
                public void onUpdatesDownloaded(HashMap<ServerFormDetails, String> result) {
                    Intent intent = new Intent(context, NotificationActivity.class);
                    intent.putExtra(NotificationActivity.NOTIFICATION_TITLE, context.getString(R.string.download_forms_result));
                    intent.putExtra(NotificationActivity.NOTIFICATION_MESSAGE, FormDownloadListActivity.getDownloadResultMessage(result));
                    PendingIntent contentIntent = PendingIntent.getActivity(context, FORMS_DOWNLOADED_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    showNotification(
                            context,
                            notificationManager,
                            R.string.odk_auto_download_notification_title,
                            context.getString(allFormsDownloadedSuccessfully(context, result) ?
                                    R.string.success :
                                    R.string.failures),
                            contentIntent,
                            FORM_UPDATE_NOTIFICATION_ID
                    );
                }
            };

            new ServerFormsUpdateNotifier(
                    multiFormDownloader,
                    serverFormsDetailsFetcher,
                    new DatabaseNotificationRepository(),
                    notifier
            ).checkAndNotify();
        };
    }

    private boolean allFormsDownloadedSuccessfully(Context context, HashMap<ServerFormDetails, String> result) {
        for (Map.Entry<ServerFormDetails, String> item : result.entrySet()) {
            if (!item.getValue().equals(context.getString(R.string.success))) {
                return false;
            }
        }
        return true;
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

    private static class DatabaseNotificationRepository implements NotificationRepository {

        @Override
        public void markFormUpdateNotified(String formId, String formHash, String manifestHash) {
            String formVersionHash = MultiFormDownloader.getMd5Hash(formHash) + manifestHash;

            ContentValues values = new ContentValues();
            values.put(LAST_DETECTED_FORM_VERSION_HASH, formVersionHash);
            new FormsDao().updateForm(values, JR_FORM_ID + "=?", new String[]{formId});
        }

        @Override
        public boolean hasFormUpdateBeenNotified(String formHash, String manifestHash) {
            String formVersionHash = MultiFormDownloader.getMd5Hash(formHash) + manifestHash;

            Cursor cursor = new FormsDao().getFormsCursor(LAST_DETECTED_FORM_VERSION_HASH + "=?", new String[]{formVersionHash});
            return cursor == null || cursor.getCount() > 0;
        }
    }
}