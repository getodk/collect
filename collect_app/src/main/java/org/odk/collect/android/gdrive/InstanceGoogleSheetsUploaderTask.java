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

package org.odk.collect.android.gdrive;

import static org.odk.collect.android.analytics.AnalyticsEvents.SUBMISSION;
import static org.odk.collect.android.utilities.InstanceUploaderUtils.DEFAULT_SUCCESSFUL_TEXT;
import static org.odk.collect.android.utilities.InstanceUploaderUtils.SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_GOOGLE_SHEETS_URL;
import static org.odk.collect.strings.localization.LocalizedApplicationKt.getLocalizedString;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.tasks.InstanceUploaderTask;
import org.odk.collect.android.upload.UploadException;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstanceUploaderUtils;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.settings.keys.ProjectKeys;

import java.util.List;

import timber.log.Timber;

public class InstanceGoogleSheetsUploaderTask extends InstanceUploaderTask {

    private final GoogleApiProvider googleApiProvider;
    private final Analytics analytics;

    public InstanceGoogleSheetsUploaderTask(GoogleApiProvider googleApiProvider, Analytics analytics) {
        this.googleApiProvider = googleApiProvider;
        this.analytics = analytics;
    }

    @Override
    protected Outcome doInBackground(Long... instanceIdsToUpload) {
        String account = settingsProvider
                .getUnprotectedSettings()
                .getString(ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT);

        InstanceGoogleSheetsUploader uploader = new InstanceGoogleSheetsUploader(googleApiProvider.getDriveApi(account), googleApiProvider.getSheetsApi(account));
        final Outcome outcome = new Outcome();

        List<Instance> instancesToUpload = uploader.getInstancesFromIds(instanceIdsToUpload);

        for (int i = 0; i < instancesToUpload.size(); i++) {
            Instance instance = instancesToUpload.get(i);

            if (isCancelled()) {
                outcome.messagesByInstanceId.put(instance.getDbId().toString(),
                        getLocalizedString(Collect.getInstance(), R.string.instance_upload_cancelled));
                return outcome;
            }

            publishProgress(i + 1, instancesToUpload.size());

            // Get corresponding blank form and verify there is exactly 1
            List<Form> forms = new FormsRepositoryProvider(Collect.getInstance()).get().getAllByFormIdAndVersion(instance.getFormId(), instance.getFormVersion());

            if (forms.size() != 1) {
                outcome.messagesByInstanceId.put(instance.getDbId().toString(),
                        getLocalizedString(Collect.getInstance(), R.string.not_exactly_one_blank_form_for_this_form_id));
            } else {
                try {
                    String destinationUrl = uploader.getUrlToSubmitTo(instance, null, null, settingsProvider.getUnprotectedSettings().getString(KEY_GOOGLE_SHEETS_URL));
                    if (InstanceUploaderUtils.doesUrlRefersToGoogleSheetsFile(destinationUrl)) {
                        uploader.uploadOneSubmission(instance, destinationUrl);
                        outcome.messagesByInstanceId.put(instance.getDbId().toString(), DEFAULT_SUCCESSFUL_TEXT);

                        analytics.logEvent(SUBMISSION, "HTTP-Sheets", Collect.getFormIdentifierHash(instance.getFormId(), instance.getFormVersion()));
                    } else {
                        outcome.messagesByInstanceId.put(instance.getDbId().toString(), SPREADSHEET_UPLOADED_TO_GOOGLE_DRIVE);
                    }
                } catch (UploadException e) {
                    Timber.d(e);
                    outcome.messagesByInstanceId.put(instance.getDbId().toString(),
                            e.getDisplayMessage());
                }
            }
        }
        return outcome;
    }
}
