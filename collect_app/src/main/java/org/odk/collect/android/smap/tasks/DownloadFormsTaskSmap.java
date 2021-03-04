package org.odk.collect.android.smap.tasks;

import android.os.AsyncTask;

import org.odk.collect.android.formmanagement.ServerFormDownloader;
import org.odk.collect.android.listeners.DownloadFormsTaskListener;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.smap.formmanagement.MultiFormDownloaderSmap;

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
public class DownloadFormsTaskSmap extends
        AsyncTask<ArrayList<ServerFormDetails>, String, HashMap<ServerFormDetails, String>> implements FormDownloaderListener {

    private final MultiFormDownloaderSmap multiFormDownloader;
    private DownloadFormsTaskListener stateListener;

    public DownloadFormsTaskSmap(MultiFormDownloaderSmap multiFormDownloader) {
        this.multiFormDownloader = multiFormDownloader;
    }

    @Override
    public void progressUpdate(String currentFile, String progress, String total) {
        publishProgress(currentFile, progress, total);
    }

    @Override
    public boolean isTaskCancelled() {
        return isCancelled();
    }

    @Override
    public HashMap<ServerFormDetails, String> doInBackground(ArrayList<ServerFormDetails>... values) {  // smap make public
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

