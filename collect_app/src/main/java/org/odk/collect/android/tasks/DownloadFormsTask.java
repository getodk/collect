/*
 * Copyright (C) 2009 University of Washington
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

import static org.odk.collect.strings.localization.LocalizedApplicationKt.getLocalizedString;

import android.os.AsyncTask;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.formmanagement.FormsDataService;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.download.FormDownloadException;
import org.odk.collect.android.listeners.DownloadFormsTaskListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * Background task for downloading a given list of forms. We assume right now that the forms are
 * coming from the same server that presented the form list, but theoretically that won't always be
 * true.
 *
 * @author msundt
 * @author carlhartung
 *
 * @deprecated Server form should be downloaded using {@link FormsDataService}
 */
public class DownloadFormsTask extends
        AsyncTask<ArrayList<ServerFormDetails>, String, Map<ServerFormDetails, FormDownloadException>> {

    private final String projectId;
    private final FormsDataService formsDataService;
    private DownloadFormsTaskListener stateListener;

    public DownloadFormsTask(String projectId, FormsDataService formsDataService) {
        this.projectId = projectId;
        this.formsDataService = formsDataService;
    }

    @Override
    protected Map<ServerFormDetails, FormDownloadException> doInBackground(ArrayList<ServerFormDetails>... values) {
        return formsDataService.downloadForms(projectId, values[0], (index, count) -> {
            ServerFormDetails serverFormDetails = values[0].get(index);
            String message = getLocalizedString(Collect.getInstance(), org.odk.collect.strings.R.string.form_download_progress,
                    serverFormDetails.getFormName(),
                    String.valueOf(count),
                    String.valueOf(serverFormDetails.getManifest().getMediaFiles().size())
            );

            publishProgress(message, String.valueOf(index), String.valueOf(values[0].size()));
            return null;
        }, this::isCancelled);
    }

    @Override
    protected void onCancelled(Map<ServerFormDetails, FormDownloadException> formDetailsStringHashMap) {
        synchronized (this) {
            if (stateListener != null) {
                stateListener.formsDownloadingCancelled();
            }
        }
    }

    @Override
    protected void onPostExecute(Map<ServerFormDetails, FormDownloadException> value) {
        synchronized (this) {
            if (stateListener != null) {
                stateListener.formsDownloadingComplete(value);
            }
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        synchronized (this) {
            if (stateListener != null) {
                // update progress and total
                stateListener.progressUpdate(values[0],
                        Integer.parseInt(values[1]),
                        Integer.parseInt(values[2]));
            }
        }

    }

    public void setDownloaderListener(DownloadFormsTaskListener sl) {
        synchronized (this) {
            stateListener = sl;
        }
    }
}
