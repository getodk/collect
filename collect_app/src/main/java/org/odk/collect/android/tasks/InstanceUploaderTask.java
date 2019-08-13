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

import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.upload.InstanceServerUploader;
import org.odk.collect.android.utilities.ApplicationConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public abstract class InstanceUploaderTask extends AsyncTask<Long, Integer, InstanceUploaderTask.Outcome> {

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
                    Set<String> keys = outcome.messagesByInstanceId.keySet();
                    Iterator<String> it = keys.iterator();
                    int count = keys.size();
                    while (count > 0) {
                        String[] selectionArgs;
                        if (count > ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER - 1) {
                            selectionArgs = new String[
                                    ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER];
                        } else {
                            selectionArgs = new String[count + 1];
                        }

                        StringBuilder selection = new StringBuilder();

                        selection.append(InstanceProviderAPI.InstanceColumns._ID + " IN (");
                        int i = 0;

                        while (it.hasNext() && i < selectionArgs.length - 1) {
                            selectionArgs[i] = it.next();
                            selection.append('?');

                            if (i != selectionArgs.length - 2) {
                                selection.append(',');
                            }
                            i++;
                        }

                        count -= selectionArgs.length - 1;
                        selection.append(") and status=?");
                        selectionArgs[i] = InstanceProviderAPI.STATUS_SUBMITTED;

                        try (Cursor results = new InstancesDao().getInstancesCursor(selection.toString(),
                                selectionArgs)) {
                            if (results != null && results.getCount() > 0) {
                                List<Long> toDelete = new ArrayList<>();
                                results.moveToPosition(-1);

                                boolean isFormAutoDeleteOptionEnabled = (boolean) GeneralSharedPreferences.getInstance().get(GeneralKeys.KEY_DELETE_AFTER_SEND);

                                // The custom configuration from the third party app overrides
                                // the app preferences set for delete after submission
                                if (deleteInstanceAfterSubmission != null) {
                                    isFormAutoDeleteOptionEnabled = deleteInstanceAfterSubmission;
                                }

                                String formId;
                                while (results.moveToNext()) {
                                    formId = results.getString(results.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                                    if (InstanceServerUploader.formShouldBeAutoDeleted(formId, isFormAutoDeleteOptionEnabled)) {
                                        toDelete.add(results.getLong(results.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
                                    }
                                }

                                DeleteInstancesTask dit = new DeleteInstancesTask();
                                dit.setContentResolver(Collect.getInstance().getContentResolver());
                                dit.execute(toDelete.toArray(new Long[toDelete.size()]));
                            }
                        } catch (SQLException e) {
                            Timber.e(e);
                        }
                    }
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

    /**
     * Represents the results of a submission attempt triggered by explicit user action (as opposed
     * to auto-send). A submission attempt can include finalized forms going to several different
     * servers because the app-level server configuration can be overridden by the blank form.
     *
     * The user-facing message that describes the result of a submission attempt for each specific
     * finalized form is written messages to {@link #messagesByInstanceId}. In the case of an
     * authentication request from the server, {@link #authRequestingServer} is set instead.
     */
    static class Outcome {
        /**
         * The URI for the server that requested authentication when the latest finalized form was
         * attempted to be sent. This URI may not match the server specified in the app settings or
         * the blank form because there could have been a redirect. It is included in the Outcome so
         * that it can be shown to the user so s/he will know where the auth request came from.
         *
         * When this field is set, the overall submission attempt is halted so that the user can be
         * asked for credentials. Once credentials are provided, the submission attempt resumes.
         */
        Uri authRequestingServer;


        /**
         * Map of database IDs for finalized forms to the user-facing status message for the latest
         * submission attempt. Currently this can be either a localized message in the case of a
         * common status or an English message in the case of a rare status that is needed for
         * developer troubleshooting.
         *
         * The keys in the map are also used to identify filled forms that were part of the ongoing
         * submission attempt and don't need to be retried in the case of an authentication request.
         * See {@link #authRequestingServer}.
         *
         * TODO: Consider mapping to something machine-readable like a message ID or status ID
         * instead of a mix of localized and non-localized user-facing strings.
         */
        HashMap<String, String> messagesByInstanceId = new HashMap<>();
    }
}
