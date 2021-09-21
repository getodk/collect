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

import android.os.AsyncTask;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.formmanagement.FormDownloadException;
import org.odk.collect.android.formmanagement.FormDownloadExceptionMapper;
import org.odk.collect.android.formmanagement.FormDownloader;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.listeners.DownloadFormsTaskListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Background task for downloading a given list of forms. We assume right now that the forms are
 * coming from the same server that presented the form list, but theoretically that won't always be
 * true.
 *
 * @author msundt
 * @author carlhartung
 */
public class DownloadFormsTask extends
        AsyncTask<ArrayList<ServerFormDetails>, String, Map<ServerFormDetails, String>> {

    private final FormDownloader formDownloader;
    private DownloadFormsTaskListener stateListener;

    public DownloadFormsTask(FormDownloader formDownloader) {
        this.formDownloader = formDownloader;
    }

    @Override
    protected Map<ServerFormDetails, String> doInBackground(ArrayList<ServerFormDetails>... values) {
        HashMap<ServerFormDetails, String> results = new HashMap<>();

        FormDownloadExceptionMapper exceptionMapper = new FormDownloadExceptionMapper(Collect.getInstance());

        int index = 1;
        for (ServerFormDetails serverFormDetails : values[0]) {
            try {
                String currentFormNumber = String.valueOf(index);
                String totalForms = String.valueOf(values[0].size());
                publishProgress(serverFormDetails.getFormName(), currentFormNumber, totalForms);

                formDownloader.downloadForm(serverFormDetails, count -> {
                    String message = Collect.getInstance().getString(R.string.form_download_progress,
                            serverFormDetails.getFormName(),
                            String.valueOf(count),
                            String.valueOf(serverFormDetails.getManifest().getMediaFiles().size())
                    );

                    publishProgress(message, currentFormNumber, totalForms);
                }, this::isCancelled);

                results.put(serverFormDetails, Collect.getInstance().getString(R.string.success));
            } catch (FormDownloadException.DownloadingInterrupted e) {
                return emptyMap();
            } catch (FormDownloadException e) {
                results.put(serverFormDetails, exceptionMapper.getMessage(e));
            }

            index++;
        }

        return results;
    }

    @Override
    protected void onCancelled(Map<ServerFormDetails, String> formDetailsStringHashMap) {
        synchronized (this) {
            if (stateListener != null) {
                stateListener.formsDownloadingCancelled();
            }
        }
    }

    @Override
    protected void onPostExecute(Map<ServerFormDetails, String> value) {
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
