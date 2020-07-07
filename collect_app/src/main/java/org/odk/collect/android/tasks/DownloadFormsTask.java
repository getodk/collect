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

import org.odk.collect.android.listeners.DownloadFormsTaskListener;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.utilities.MultiFormDownloader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Background task for downloading a given list of forms. We assume right now that the forms are
 * coming from the same server that presented the form list, but theoretically that won't always be
 * true.
 *
 * @author msundt
 * @author carlhartung
 */
public class DownloadFormsTask extends
        AsyncTask<ArrayList<ServerFormDetails>, String, HashMap<ServerFormDetails, String>> implements FormDownloaderListener {

    private DownloadFormsTaskListener stateListener;

    @Override
    public void progressUpdate(String currentFile, String progress, String total) {
        publishProgress(currentFile, progress, total);
    }

    @Override
    public boolean isTaskCanceled() {
        return isCancelled();
    }

    @Override
    protected HashMap<ServerFormDetails, String> doInBackground(ArrayList<ServerFormDetails>... values) {
        MultiFormDownloader multiFormDownloader = new MultiFormDownloader();
        return multiFormDownloader.downloadForms(values[0], this);
    }

    @Override
    protected void onCancelled(HashMap<ServerFormDetails, String> formDetailsStringHashMap) {
        synchronized (this) {
            if (stateListener != null) {
                stateListener.formsDownloadingCancelled();
            }
        }
    }

    @Override
    protected void onPostExecute(HashMap<ServerFormDetails, String> value) {
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
