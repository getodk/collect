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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.activities.NotificationActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.utilities.DownloadFormListUtils;
import org.odk.collect.android.utilities.FormDownloader;
import org.odk.collect.android.utilities.IconUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.odk.collect.android.activities.FormDownloadList.DISPLAY_ONLY_UPDATED_FORMS;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOMATIC_UPDATE;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORM_UPDATES_AVAILABLE_NOTIFICATION;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORMS_DOWNLOADED_NOTIFICATION;
import static org.odk.collect.android.utilities.DownloadFormListUtils.DL_AUTH_REQUIRED;
import static org.odk.collect.android.utilities.DownloadFormListUtils.DL_ERROR_MSG;

public class ServerPollingJob extends Job {

    private static final long FIFTEEN_MINUTES_PERIOD = 900000;
    private static final long ONE_HOUR_PERIOD = 3600000;
    private static final long SIX_HOURS_PERIOD = 21600000;
    private static final long ONE_DAY_PERIOD = 86400000;

    private static final String POLL_SERVER_IMMEDIATELY_AFTER_RECEIVING_NETWORK = "pollServerImmediatelyAfterReceivingNetwork";
    public static final String TAG = "serverPollingJob";

    @Override
    @NonNull
    protected Result onRunJob(@NonNull Params params) {
        if (!isDeviceOnline()) {
            GeneralSharedPreferences.getInstance().save(POLL_SERVER_IMMEDIATELY_AFTER_RECEIVING_NETWORK, true);
            return Result.FAILURE;
        } else {
            GeneralSharedPreferences.getInstance().reset(POLL_SERVER_IMMEDIATELY_AFTER_RECEIVING_NETWORK);
            HashMap<String, FormDetails> formList = DownloadFormListUtils.downloadFormList(true);

            if (formList != null && !formList.containsKey(DL_ERROR_MSG)) {
                if (formList.containsKey(DL_AUTH_REQUIRED)) {
                    AuthDialogUtility.setWebCredentialsFromPreferences();
                    formList = DownloadFormListUtils.downloadFormList(true);

                    if (formList == null || formList.containsKey(DL_AUTH_REQUIRED) || formList.containsKey(DL_ERROR_MSG)) {
                        return Result.FAILURE;
                    }
                }

                List<FormDetails> newDetectedForms = new ArrayList<>();
                for (FormDetails formDetails : formList.values()) {
                    if (formDetails.isNewerFormVersionAvailable() || formDetails.areNewerMediaFilesAvailable()) {
                        newDetectedForms.add(formDetails);
                    }
                }

                if (!newDetectedForms.isEmpty()) {
                    if (GeneralSharedPreferences.getInstance().getBoolean(KEY_AUTOMATIC_UPDATE, false)) {
                        final HashMap<FormDetails, String> result = new FormDownloader().downloadForms(newDetectedForms);
                        informAboutNewDownloadedForms(Collect.getInstance().getString(R.string.download_forms_result), result);
                    } else {
                        for (FormDetails formDetails : newDetectedForms) {
                            String manifestFileHash = formDetails.getManifestFileHash() != null ? formDetails.getManifestFileHash() : "";
                            String formVersionHash = FormDownloader.getMd5Hash(formDetails.getHash()) + manifestFileHash;
                            if (!wasThisNewerFormVersionAlreadyDetected(formVersionHash)) {
                                updateLastDetectedFormVersionHash(formDetails.getFormID(), formVersionHash);
                            } else {
                                newDetectedForms.remove(formDetails);
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
    }

    public static void schedulePeriodicJob(String selectedOption) {
        if (selectedOption.equals(Collect.getInstance().getString(R.string.never_value))) {
            JobManager.instance().cancelAllForTag(TAG);
            GeneralSharedPreferences.getInstance().reset(POLL_SERVER_IMMEDIATELY_AFTER_RECEIVING_NETWORK);
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
                    .build()
                    .schedule();
        }
    }

    private boolean wasThisNewerFormVersionAlreadyDetected(String formVersionHash) {
        Cursor cursor = new FormsDao().getFormsCursor(LAST_DETECTED_FORM_VERSION_HASH + "=?", new String[]{formVersionHash});
        return cursor == null || cursor.getCount() > 0;
    }

    private void informAboutNewAvailableForms() {
        Intent intent = new Intent(getContext(), FormDownloadList.class);
        intent.putExtra(DISPLAY_ONLY_UPDATED_FORMS, true);
        PendingIntent contentIntent = PendingIntent.getActivity(getContext(), FORM_UPDATES_AVAILABLE_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
                .setSmallIcon(IconUtils.getNotificationAppIcon())
                .setContentTitle(getContext().getString(R.string.form_updates_available))
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(0, builder.build());
        }
    }

    private void informAboutNewDownloadedForms(String title, HashMap<FormDetails, String> result) {
        Intent intent = new Intent(Collect.getInstance(), NotificationActivity.class);
        intent.putExtra(NotificationActivity.NOTIFICATION_TITLE, title);
        intent.putExtra(NotificationActivity.NOTIFICATION_MESSAGE, FormDownloadList.getDownloadResultMessage(result));
        PendingIntent contentIntent = PendingIntent.getActivity(getContext(), FORMS_DOWNLOADED_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
                .setSmallIcon(IconUtils.getNotificationAppIcon())
                .setContentTitle(getContext().getString(R.string.odk_auto_download_notification_title))
                .setContentText(getContentText(result))
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(0, builder.build());
        }
    }

    private void updateLastDetectedFormVersionHash(String formId, String formVersionHash) {
        ContentValues values = new ContentValues();
        values.put(LAST_DETECTED_FORM_VERSION_HASH, formVersionHash);
        new FormsDao().updateForm(values, JR_FORM_ID + "=?", new String[] {formId});
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) Collect.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static void pollServerIfNeeded() {
        if (GeneralSharedPreferences.getInstance().getBoolean(POLL_SERVER_IMMEDIATELY_AFTER_RECEIVING_NETWORK, false)
                && !GeneralSharedPreferences.getInstance().get(KEY_PERIODIC_FORM_UPDATES_CHECK).equals(Collect.getInstance().getString(R.string.never_value))) {
            new JobRequest.Builder(TAG)
                    .startNow()
                    .build()
                    .schedule();
        }
    }

    private String getContentText(HashMap<FormDetails, String> result) {
        return allFormsDownloadedSuccessfully(result)
                ? Collect.getInstance().getString(R.string.success)
                : Collect.getInstance().getString(R.string.failures);
    }

    private boolean allFormsDownloadedSuccessfully(HashMap<FormDetails, String> result) {
        for (Map.Entry<FormDetails, String> item : result.entrySet()) {
            if (!item.getValue().equals(Collect.getInstance().getString(R.string.success))) {
                return false;
            }
        }
        return true;
    }
}