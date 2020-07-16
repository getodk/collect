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

package org.odk.collect.android.formmanagement;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormDownloadListActivity;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.utilities.MultiFormDownloader;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import static org.odk.collect.android.activities.FormDownloadListActivity.DISPLAY_ONLY_UPDATED_FORMS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_AUTOMATIC_UPDATE;
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

            try {
                List<ServerFormDetails> newDetectedForms = fetchUpdatedForms();

                if (!newDetectedForms.isEmpty()) {
                    if (GeneralSharedPreferences.getInstance().getBoolean(KEY_AUTOMATIC_UPDATE, false)) {
                        final HashMap<ServerFormDetails, String> result = multiFormDownloader.downloadForms(newDetectedForms, null);
                        notifyAboutDownloadedForms(context.getString(R.string.download_forms_result), result, context);
                    } else {
                        notifyAboutUpdatedForms(newDetectedForms, context);
                    }
                }
            } catch (FormApiException e) {
                return;
            }
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

    private void notifyAboutUpdatedForms(List<ServerFormDetails> newDetectedForms, Context context) {
        boolean needsNotification = false;

        for (ServerFormDetails serverFormDetails : newDetectedForms) {
            String formHash = serverFormDetails.getHash();
            String manifestFileHash = serverFormDetails.getManifestFileHash() != null ? serverFormDetails.getManifestFileHash() : "";

            if (!notificationRepository.hasFormUpdateBeenNotified(formHash, manifestFileHash)) {
                needsNotification = true;
                notificationRepository.markFormUpdateNotified(serverFormDetails.getFormId(), formHash, manifestFileHash);
            }
        }

        if (needsNotification) {
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
    }

    @Nullable
    @SuppressWarnings("PMD.AvoidRethrowingException")
    private List<ServerFormDetails> fetchUpdatedForms() throws FormApiException {
        List<ServerFormDetails> formList = null;

        try {
            formList = serverFormsDetailsFetcher.fetchFormDetails();
        } catch (FormApiException e) {
            switch (e.getType()) {
                case AUTH_REQUIRED:
                    try {
                        serverFormsDetailsFetcher.fetchFormDetails();
                    } catch (FormApiException ex) {
                        throw ex;
                    }

                    break;

                case FETCH_ERROR:
                case PARSE_ERROR:
                case LEGACY_PARSE_ERROR:
                    throw e;
            }
        }

        List<ServerFormDetails> newDetectedForms = new ArrayList<>();
        for (ServerFormDetails serverFormDetails : formList) {
            if (serverFormDetails.isUpdated()) {
                newDetectedForms.add(serverFormDetails);
            }
        }

        return newDetectedForms;
    }

    private void notifyAboutDownloadedForms(String title, HashMap<ServerFormDetails, String> result, @NotNull Context context) {
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra(NotificationActivity.NOTIFICATION_TITLE, title);
        intent.putExtra(NotificationActivity.NOTIFICATION_MESSAGE, FormDownloadListActivity.getDownloadResultMessage(result));
        PendingIntent contentIntent = PendingIntent.getActivity(context, FORMS_DOWNLOADED_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        showNotification(
                context,
                notificationManager,
                R.string.odk_auto_download_notification_title,
                getContentText(context, result),
                contentIntent,
                FORM_UPDATE_NOTIFICATION_ID
        );
    }

    private String getContentText(Context context, HashMap<ServerFormDetails, String> result) {
        return allFormsDownloadedSuccessfully(context, result)
                ? context.getString(R.string.success)
                : context.getString(R.string.failures);
    }

    private boolean allFormsDownloadedSuccessfully(Context context, HashMap<ServerFormDetails, String> result) {
        for (Map.Entry<ServerFormDetails, String> item : result.entrySet()) {
            if (!item.getValue().equals(context.getString(R.string.success))) {
                return false;
            }
        }
        return true;
    }

    private static class DatabaseNotificationRepository {

        public void markFormUpdateNotified(String formId, String formHash, String manifestHash) {
            String formVersionHash = MultiFormDownloader.getMd5Hash(formHash) + manifestHash;

            ContentValues values = new ContentValues();
            values.put(LAST_DETECTED_FORM_VERSION_HASH, formVersionHash);
            new FormsDao().updateForm(values, JR_FORM_ID + "=?", new String[] {formId});
        }

        public boolean hasFormUpdateBeenNotified(String formHash, String manifestHash) {
            String formVersionHash = MultiFormDownloader.getMd5Hash(formHash) + manifestHash;

            Cursor cursor = new FormsDao().getFormsCursor(LAST_DETECTED_FORM_VERSION_HASH + "=?", new String[]{formVersionHash});
            return cursor == null || cursor.getCount() > 0;
        }
    }
}