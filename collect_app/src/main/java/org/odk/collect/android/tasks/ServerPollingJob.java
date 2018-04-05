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
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.utilities.DownloadFormListUtils;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.WebUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.odk.collect.android.activities.FormDownloadList.DISPLAY_ONLY_UPDATED_FORMS;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.JR_FORM_ID;
import static org.odk.collect.android.provider.FormsProviderAPI.FormsColumns.LAST_DETECTED_FORM_VERSION_HASH;
import static org.odk.collect.android.utilities.DownloadFormListUtils.DL_AUTH_REQUIRED;
import static org.odk.collect.android.utilities.DownloadFormListUtils.DL_ERROR_MSG;

public class ServerPollingJob extends Job {

    private static final long FIFTEEN_MINUTES_PERIOD = 900000;
    private static final long ONE_HOUR_PERIOD = 3600000;
    private static final long SIX_HOURS_PERIOD = 21600000;
    private static final long ONE_DAY_PERIOD = 86400000;

    public static final String TAG = "serverPollingJob";

    @Override
    @NonNull
    protected Result onRunJob(@NonNull Params params) {
        HashMap<String, FormDetails> formList = DownloadFormListUtils.downloadFormList();

        if (formList != null && !formList.containsKey(DL_ERROR_MSG)) {
            if (formList.containsKey(DL_AUTH_REQUIRED)) {
                AuthDialogUtility.setWebCredentialsFromPreferences();
                formList = DownloadFormListUtils.downloadFormList();

                if (formList == null || formList.containsKey(DL_AUTH_REQUIRED) || formList.containsKey(DL_ERROR_MSG)) {
                    return Result.FAILURE;
                }
            }

            List<FormDetails> newDetectedForms = new ArrayList<>();
            for (FormDetails formDetails : formList.values()) {
                if (formDetails.isNewerFormVersionAvailable() || formDetails.areNewerMediaFilesAvailable()) {
                    String manifestFileHash = formDetails.getManifestUrl() != null ? FileUtils.getMd5Hash(WebUtils.getFileInputStream(formDetails.getManifestUrl())) : "";

                    String formVersionHash = DownloadFormsTask.getMd5Hash(formDetails.getHash()) + manifestFileHash;
                     if (!wasThisNewerFormVersionAlreadyDetected(formVersionHash)) {
                         newDetectedForms.add(formDetails);
                         updateLastDetectedFormVersionHash(formDetails.getFormID(), formVersionHash);
                    }
                }
            }

            if (!newDetectedForms.isEmpty()) {
                newFormVersionDetected(newDetectedForms);
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
            } else if (selectedOption.equals(Collect.getInstance().getString(R.string.every_one_day_value))) {
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

    private void newFormVersionDetected(List<FormDetails> newerForms) {
        StringBuilder listOfForms = new StringBuilder();
        for (FormDetails formDetails : newerForms) {
            listOfForms.append(formDetails.getFormName());

            if (newerForms.indexOf(formDetails) < newerForms.size() - 1) {
                listOfForms.append(", ");
            }
        }
        showNotification(listOfForms.toString());
    }

    private void showNotification(String message) {
        Intent intent = new Intent(getContext(), FormDownloadList.class);
        intent.putExtra(DISPLAY_ONLY_UPDATED_FORMS, true);
        PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
                .setSmallIcon(R.drawable.ic_info)
                .setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.notes))
                .setContentTitle(getContext().getString(R.string.form_updates_available))
                .setContentText(message)
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
}