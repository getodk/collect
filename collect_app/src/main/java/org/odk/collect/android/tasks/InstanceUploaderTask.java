/*
 * Copyright 2016 Nafundi
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

import static org.odk.collect.android.utilities.InstanceUploaderUtils.shouldFormBeDeleted;

import android.net.Uri;
import android.os.AsyncTask;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.settings.keys.ProjectKeys;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

public abstract class InstanceUploaderTask extends AsyncTask<Long, Integer, InstanceUploaderTask.Outcome> {

    private InstancesRepository instancesRepository;
    private FormsRepository formsRepository;
    protected SettingsProvider settingsProvider;
    private InstanceUploaderListener stateListener;
    private Boolean deleteInstanceAfterSubmission;

    @Override
    protected void onPostExecute(Outcome outcome) {
        synchronized (this) {
            if (outcome != null && stateListener != null) {
                if (outcome.authRequestingServer != null) {
                    stateListener.authRequest(outcome.authRequestingServer, outcome.messagesByInstanceId);
                } else {
                    stateListener.uploadingComplete(outcome.messagesByInstanceId);

                    // Delete instances that were successfully sent and that need to be deleted
                    // either because app-level auto-delete is enabled or because the form
                    // specifies it.
                    Set<String> instanceIds = outcome.messagesByInstanceId.keySet();

                    boolean isFormAutoDeleteOptionEnabled;

                    // The custom configuration from the third party app overrides
                    // the app preferences set for delete after submission
                    if (deleteInstanceAfterSubmission != null) {
                        isFormAutoDeleteOptionEnabled = deleteInstanceAfterSubmission;
                    } else {
                        isFormAutoDeleteOptionEnabled = settingsProvider.getUnprotectedSettings().getBoolean(ProjectKeys.KEY_DELETE_AFTER_SEND);
                    }

                    Stream<Instance> instancesToDelete = instanceIds.stream()
                            .map(id -> new InstancesRepositoryProvider(Collect.getInstance()).get().get(Long.parseLong(id)))
                            .filter(instance -> instance.getStatus().equals(Instance.STATUS_SUBMITTED))
                            .filter(instance -> shouldFormBeDeleted(formsRepository, instance.getFormId(), instance.getFormVersion(), isFormAutoDeleteOptionEnabled));

                    DeleteInstancesTask dit = new DeleteInstancesTask(instancesRepository, formsRepository);
                    dit.execute(instancesToDelete.map(Instance::getDbId).toArray(Long[]::new));
                }
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        synchronized (this) {
            if (stateListener != null) {
                stateListener.progressUpdate(values[0], values[1]);
            }
        }
    }

    public void setUploaderListener(InstanceUploaderListener sl) {
        synchronized (this) {
            stateListener = sl;
        }
    }

    public void setDeleteInstanceAfterSubmission(Boolean deleteInstanceAfterSubmission) {
        this.deleteInstanceAfterSubmission = deleteInstanceAfterSubmission;
    }

    public void setRepositories(InstancesRepository instancesRepository, FormsRepository formsRepository, SettingsProvider settingsProvider) {
        this.instancesRepository = instancesRepository;
        this.formsRepository = formsRepository;
        this.settingsProvider = settingsProvider;
    }

    /**
     * Represents the results of a submission attempt triggered by explicit user action (as opposed
     * to auto-send). A submission attempt can include finalized forms going to several different
     * servers because the app-level server configuration can be overridden by the blank form.
     * <p>
     * The user-facing message that describes the result of a submission attempt for each specific
     * finalized form is written messages to {@link #messagesByInstanceId}. In the case of an
     * authentication request from the server, {@link #authRequestingServer} is set instead.
     */
    public static class Outcome {
        /**
         * The URI for the server that requested authentication when the latest finalized form was
         * attempted to be sent. This URI may not match the server specified in the app settings or
         * the blank form because there could have been a redirect. It is included in the Outcome so
         * that it can be shown to the user so s/he will know where the auth request came from.
         * <p>
         * When this field is set, the overall submission attempt is halted so that the user can be
         * asked for credentials. Once credentials are provided, the submission attempt resumes.
         */
        public Uri authRequestingServer;


        /**
         * Map of database IDs for finalized forms to the user-facing status message for the latest
         * submission attempt. Currently this can be either a localized message in the case of a
         * common status or an English message in the case of a rare status that is needed for
         * developer troubleshooting.
         * <p>
         * The keys in the map are also used to identify filled forms that were part of the ongoing
         * submission attempt and don't need to be retried in the case of an authentication request.
         * See {@link #authRequestingServer}.
         * <p>
         * TODO: Consider mapping to something machine-readable like a message ID or status ID
         * instead of a mix of localized and non-localized user-facing strings.
         */
        public HashMap<String, String> messagesByInstanceId = new HashMap<>();
    }
}
