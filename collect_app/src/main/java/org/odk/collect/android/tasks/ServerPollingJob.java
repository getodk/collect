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

package org.odk.collect.android.tasks;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormDownloadListActivity;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.utilities.FormListDownloader;
import org.odk.collect.android.utilities.MultiFormDownloader;
import org.odk.collect.android.utilities.NotificationUtils;

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
import static org.odk.collect.android.utilities.FormListDownloader.DL_AUTH_REQUIRED;
import static org.odk.collect.android.utilities.FormListDownloader.DL_ERROR_MSG;
import static org.odk.collect.android.utilities.NotificationUtils.FORM_UPDATE_NOTIFICATION_ID;

public class ServerPollingJob extends Job {

    private static final long FIFTEEN_MINUTES_PERIOD = 900000;
    private static final long ONE_HOUR_PERIOD = 3600000;
    private static final long SIX_HOURS_PERIOD = 21600000;
    private static final long ONE_DAY_PERIOD = 86400000;

    public static final String TAG = "serverPollingJob";

    @Inject
    FormListDownloader formListDownloader;

    @Inject
    StorageMigrationRepository storageMigrationRepository;

    @Inject
    NetworkStateProvider connectivityProvider;

    public ServerPollingJob() {
        Collect.getInstance().getComponent().inject(this);
    }

    @Override
    @NonNull
    protected Result onRunJob(@NonNull Params params) {
        if (!connectivityProvider.isDeviceOnline() || storageMigrationRepository.isMigrationBeingPerformed()) {
            return Result.FAILURE;
        }

        HashMap<String, ServerFormDetails> formList = formListDownloader.downloadFormList(null, null, null, true);

        if (!formList.containsKey(DL_ERROR_MSG)) {
            if (formList.containsKey(DL_AUTH_REQUIRED)) {
                formList = formListDownloader.downloadFormList(null, null, null, true);

                if (formList.containsKey(DL_AUTH_REQUIRED) || formList.containsKey(DL_ERROR_MSG)) {
                    return Result.FAILURE;
                }
            }

            List<ServerFormDetails> newDetectedForms = new ArrayList<>();
            for (ServerFormDetails serverFormDetails : formList.values()) {
                if (serverFormDetails.isNewerFormVersionAvailable() || serverFormDetails.areNewerMediaFilesAvailable()) {
                    newDetectedForms.add(serverFormDetails);
                }
            }

            if (!newDetectedForms.isEmpty()) {
                if (GeneralSharedPreferences.getInstance().getBoolean(KEY_AUTOMATIC_UPDATE, false)) {
                    final HashMap<ServerFormDetails, String> result = new MultiFormDownloader().downloadForms(newDetectedForms, null);
                    informAboutNewDownloadedForms(Collect.getInstance().getString(R.string.download_forms_result), result);
                } else {
                    for (ServerFormDetails serverFormDetails : newDetectedForms) {
                        String manifestFileHash = serverFormDetails.getManifestFileHash() != null ? serverFormDetails.getManifestFileHash() : "";
                        String formVersionHash = MultiFormDownloader.getMd5Hash(serverFormDetails.getHash()) + manifestFileHash;
                        if (!wasThisNewerFormVersionAlreadyDetected(formVersionHash)) {
                            updateLastDetectedFormVersionHash(serverFormDetails.getFormId(), formVersionHash);
                        } else {
                            newDetectedForms.remove(serverFormDetails);
                        }
                    }

                    if (!newDetectedForms.isEmpty()) {
                        informAboutNewAvailableForms();
                    }
                }
            }
            return Result.SUCCESS;
        } else {
            return Result.FAILURE;
        }
    }

    public static void schedulePeriodicJob(String selectedOption) {
        if (selectedOption.equals(Collect.getInstance().getString(R.string.never_value))) {
            JobManager.instance().cancelAllForTag(TAG);
        } else {
            long period = FIFTEEN_MINUTES_PERIOD;
            if (selectedOption.equals(Collect.getInstance().getString(R.string.every_one_hour_value))) {
                period = ONE_HOUR_PERIOD;
            } else if (selectedOption.equals(Collect.getInstance().getString(R.string.every_six_hours_value))) {
                period = SIX_HOURS_PERIOD;
            } else if (selectedOption.equals(Collect.getInstance().getString(R.string.every_24_hours_value))) {
                period = ONE_DAY_PERIOD;
            }

            new JobRequest.Builder(TAG)
                    .setPeriodic(period, 300000)
                    .setUpdateCurrent(true)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .build()
                    .schedule();
        }
    }

    private boolean wasThisNewerFormVersionAlreadyDetected(String formVersionHash) {
        Cursor cursor = new FormsDao().getFormsCursor(LAST_DETECTED_FORM_VERSION_HASH + "=?", new String[]{formVersionHash});
        return cursor == null || cursor.getCount() > 0;
    }

    private void informAboutNewAvailableForms() {
        Intent intent = new Intent(getContext(), FormDownloadListActivity.class);
        intent.putExtra(DISPLAY_ONLY_UPDATED_FORMS, true);
        PendingIntent contentIntent = PendingIntent.getActivity(getContext(), FORM_UPDATES_AVAILABLE_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationUtils.showNotification(
                contentIntent,
                FORM_UPDATE_NOTIFICATION_ID,
                R.string.form_updates_available,
                null);
    }

    private void informAboutNewDownloadedForms(String title, HashMap<ServerFormDetails, String> result) {
        Intent intent = new Intent(Collect.getInstance(), NotificationActivity.class);
        intent.putExtra(NotificationActivity.NOTIFICATION_TITLE, title);
        intent.putExtra(NotificationActivity.NOTIFICATION_MESSAGE, FormDownloadListActivity.getDownloadResultMessage(result));
        PendingIntent contentIntent = PendingIntent.getActivity(getContext(), FORMS_DOWNLOADED_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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