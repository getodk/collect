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

import android.database.Cursor;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.auth.GoogleAuthException;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dto.Form;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.upload.InstanceGoogleSheetsUploader;
import org.odk.collect.android.upload.UploadException;
import org.odk.collect.android.utilities.gdrive.GoogleAccountsManager;

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

import static org.odk.collect.android.utilities.InstanceUploaderUtils.DEFAULT_SUCCESSFUL_TEXT;

public class InstanceGoogleSheetsUploaderTask extends InstanceUploaderTask {
    private final GoogleAccountsManager accountsManager;

    private boolean authFailed;

    public InstanceGoogleSheetsUploaderTask(GoogleAccountsManager accountsManager) {
        this.accountsManager = accountsManager;
    }

    @Override
    protected Outcome doInBackground(Long... instanceIdsToUpload) {
        InstanceGoogleSheetsUploader uploader = new InstanceGoogleSheetsUploader(accountsManager);
        final Outcome outcome = new Outcome();

        try {
            // User-recoverable auth error
            if (uploader.getAuthToken() == null) {
                return null;
            }
        } catch (IOException | GoogleAuthException e) {
            Timber.d(e);
            authFailed = true;
        }

        // TODO: check this behavior against master -- is there an error message shown?
        if (!uploader.submissionsFolderExistsAndIsUnique()) {
            return outcome;
        }

        List<Instance> instancesToUpload = uploader.getInstancesFromIds(instanceIdsToUpload);

        for (int i = 0; i < instancesToUpload.size(); i++) {
            Instance instance = instancesToUpload.get(i);

            if (isCancelled()) {
                outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(),
                        Collect.getInstance().getString(R.string.instance_upload_cancelled));
                return outcome;
            }

            publishProgress(i + 1, instancesToUpload.size());

            // Get corresponding blank form and verify there is exactly 1
            FormsDao dao = new FormsDao();
            Cursor formCursor = dao.getFormsCursor(instance.getJrFormId(), instance.getJrVersion());
            List<Form> forms = dao.getFormsFromCursor(formCursor);

            if (forms.size() != 1) {
                outcome.messagesByInstanceId.put(instance.getDatabaseId().toString(),
                        Collect.getInstance().getString(R.string.not_exactly_one_blank_form_for_this_form_id));
            } else {
                try {
                    String destinationUrl = uploader.getUrlToSubmitTo(instance, null, null);
                    uploader.uploadOneSubmission(instance, destinationUrl);

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
                            e.getDisplayMessage());
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