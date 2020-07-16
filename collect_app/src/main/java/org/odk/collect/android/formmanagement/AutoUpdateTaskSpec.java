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
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.utilities.MultiFormDownloader;
import org.odk.collect.android.utilities.NotificationUtils;
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

public class AutoUpdateTaskSpec implements TaskSpec {

    @Inject
    ServerFormsDetailsFetcher serverFormsDetailsFetcher;

    @Inject
    StorageMigrationRepository storageMigrationRepository;

    @Inject
    NetworkStateProvider connectivityProvider;

    @Inject
    MultiFormDownloader multiFormDownloader;

    @NotNull
    @Override
    public Runnable getTask(@NotNull Context context) {
        DaggerUtils.getComponent(context).inject(this);

        return () -> {
            if (!connectivityProvider.isDeviceOnline() || storageMigrationRepository.isMigrationBeingPerformed()) {
                return;
            }

            try {
                List<ServerFormDetails> newDetectedForms = fetchUpdatedForms();

                if (!newDetectedForms.isEmpty()) {
                    if (GeneralSharedPreferences.getInstance().getBoolean(KEY_AUTOMATIC_UPDATE, false)) {
                        final HashMap<ServerFormDetails, String> result = multiFormDownloader.downloadForms(newDetectedForms, null);
                        notifyAboutDownloadedForms(Collect.getInstance().getString(R.string.download_forms_result), result);
                    } else {
                        notifyAboutUpdatedForms(newDetectedForms);
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

    private void notifyAboutUpdatedForms(List<ServerFormDetails> newDetectedForms) {
        boolean needsNotification = false;

        for (ServerFormDetails serverFormDetails : newDetectedForms) {
            String manifestFileHash = serverFormDetails.getManifestFileHash() != null ? serverFormDetails.getManifestFileHash() : "";
            String formVersionHash = MultiFormDownloader.getMd5Hash(serverFormDetails.getHash()) + manifestFileHash;
            if (!wasThisNewerFormVersionAlreadyDetected(formVersionHash)) {
                needsNotification = true;
                updateLastDetectedFormVersionHash(serverFormDetails.getFormId(), formVersionHash);
            }
        }

        if (needsNotification) {
            Intent intent = new Intent(Collect.getInstance(), FormDownloadListActivity.class);
            intent.putExtra(DISPLAY_ONLY_UPDATED_FORMS, true);
            PendingIntent contentIntent = PendingIntent.getActivity(Collect.getInstance(), FORM_UPDATES_AVAILABLE_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationUtils.showNotification(
                    contentIntent,
                    FORM_UPDATE_NOTIFICATION_ID,
                    R.string.form_updates_available,
                    null);
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

    private boolean wasThisNewerFormVersionAlreadyDetected(String formVersionHash) {
        Cursor cursor = new FormsDao().getFormsCursor(LAST_DETECTED_FORM_VERSION_HASH + "=?", new String[]{formVersionHash});
        return cursor == null || cursor.getCount() > 0;
    }

    private void notifyAboutDownloadedForms(String title, HashMap<ServerFormDetails, String> result) {
        Intent intent = new Intent(Collect.getInstance(), NotificationActivity.class);
        intent.putExtra(NotificationActivity.NOTIFICATION_TITLE, title);
        intent.putExtra(NotificationActivity.NOTIFICATION_MESSAGE, FormDownloadListActivity.getDownloadResultMessage(result));
        PendingIntent contentIntent = PendingIntent.getActivity(Collect.getInstance(), FORMS_DOWNLOADED_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationUtils.showNotification(contentIntent,
                FORM_UPDATE_NOTIFICATION_ID,
                R.string.odk_auto_download_notification_title,
                getContentText(result));
    }

    private void updateLastDetectedFormVersionHash(String formId, String formVersionHash) {
        ContentValues values = new ContentValues();
        values.put(LAST_DETECTED_FORM_VERSION_HASH, formVersionHash);
        new FormsDao().updateForm(values, JR_FORM_ID + "=?", new String[] {formId});
    }

    private String getContentText(HashMap<ServerFormDetails, String> result) {
        return allFormsDownloadedSuccessfully(result)
                ? Collect.getInstance().getString(R.string.success)
                : Collect.getInstance().getString(R.string.failures);
    }

    private boolean allFormsDownloadedSuccessfully(HashMap<ServerFormDetails, String> result) {
        for (Map.Entry<ServerFormDetails, String> item : result.entrySet()) {
            if (!item.getValue().equals(Collect.getInstance().getString(R.string.success))) {
                return false;
            }
        }
        return true;
    }
}