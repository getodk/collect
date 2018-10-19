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

package org.odk.collect.android.tasks;

import android.app.Activity;
import android.database.Cursor;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.exception.MultipleFoldersFoundException;
import org.odk.collect.android.http.CollectServerClient.Outcome;
import org.odk.collect.android.upload.InstanceGoogleSheetsUploader;
import org.odk.collect.android.upload.result.UploadException;
import org.odk.collect.android.utilities.gdrive.DriveHelper;
import org.odk.collect.android.utilities.gdrive.GoogleAccountsManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import timber.log.Timber;

import static org.odk.collect.android.utilities.InstanceUploaderUtils.DEFAULT_SUCCESSFUL_TEXT;
import static org.odk.collect.android.utilities.gdrive.GoogleAccountsManager.REQUEST_AUTHORIZATION;

public class InstanceGoogleSheetsUploaderTask extends InstanceUploaderTask {
    public static final String GOOGLE_DRIVE_ROOT_FOLDER = "Open Data Kit";
    public static final String GOOGLE_DRIVE_SUBFOLDER = "Submissions";

    private final DriveHelper driveHelper;
    private final GoogleAccountsManager accountsManager;

    private boolean authFailed;

    public InstanceGoogleSheetsUploaderTask(GoogleAccountsManager accountsManager) {
        this.accountsManager = accountsManager;
        driveHelper = accountsManager.getDriveHelper();
    }

    @Override
    protected Outcome doInBackground(Long... instanceIdsToUpload) {
        final Outcome outcome = new Outcome();

        String token = null;
        try {
            token = accountsManager.getCredential().getToken();
        } catch (UserRecoverableAuthException e) {
            Activity activity = accountsManager.getActivity();
            if (activity != null) {
                activity.startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            }
            return null;
        } catch (IOException | GoogleAuthException e) {
            Timber.d(e);
            authFailed = true;
        }

        // Immediately invalidate so we get a different one if we have to try again
        GoogleAuthUtil.invalidateToken(accountsManager.getContext(), token);

        try {
            // check if root folder exists, if not then create one
            driveHelper.getIDOfFolderWithName(GOOGLE_DRIVE_ROOT_FOLDER, null, true);
        } catch (IOException | MultipleFoldersFoundException e) {
            Timber.d(e, "Exception getting or creating root folder for submissions");
        }

        InstanceGoogleSheetsUploader uploader = new InstanceGoogleSheetsUploader(driveHelper, accountsManager.getSheetsHelper());
        List<Instance> instancesToUpload = uploader.getInstancesFromIds(instanceIdsToUpload);

        for (int i = 0; i < instancesToUpload.size(); i++) {
            Instance instance = instancesToUpload.get(i);

            if (isCancelled()) {
                outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(),
                        Collect.getInstance().getString(R.string.instance_upload_cancelled));
                return outcome;
            }

            publishProgress(i + 1, instancesToUpload.size());

            FormsDao dao = new FormsDao();
            Cursor formCursor = dao.getFormsCursor(instance.getJrFormId(), instance.getJrVersion());
            List<Form> forms = dao.getFormsFromCursor(formCursor);

            if (forms.size() != 1) {
                outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(),
                        Collect.getInstance().getString(R.string.not_exactly_one_blank_form_for_this_form_id));
            } else {
                Form form = forms.get(0);

                try {
                    uploader.uploadOneSubmission(instance, new File(instance.getInstanceFilePath()),
                            form.getFormFilePath(), uploader.getUrlToSubmitTo(instance));

                    uploader.saveSuccessStatusToDatabase(instance);

                    outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(), DEFAULT_SUCCESSFUL_TEXT);

                    Collect.getInstance()
                            .getDefaultTracker()
                            .send(new HitBuilders.EventBuilder()
                                    .setCategory("Submission")
                                    .setAction("HTTP-Sheets")
                                    .build());
                } catch (UploadException e) {
                    Timber.d(e);
                    outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(),
                            e.getMessage() != null ? e.getMessage() : e.getCause().getMessage());

                    uploader.saveFailedStatusToDatabase(instance);
                }
            }
        }
        return outcome;
    }

    public boolean isAuthFailed() {
        return authFailed;
    }

    public void setAuthFailedToFalse() {
        authFailed = false;
    }
}