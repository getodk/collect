package org.odk.collect.android.smap.tasks;

import android.os.AsyncTask;

import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.smap.formmanagement.MultiFormDownloaderSmap;
import org.odk.collect.android.smap.formmanagement.ServerFormDetailsSmap;
import org.odk.collect.android.smap.listeners.DownloadFormsTaskListenerSmap;

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
        AsyncTask<ArrayList<ServerFormDetailsSmap>, String, HashMap<ServerFormDetailsSmap, String>> implements FormDownloaderListener {

    private final MultiFormDownloaderSmap multiFormDownloader;
    private DownloadFormsTaskListenerSmap stateListener;

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
    public HashMap<ServerFormDetailsSmap, String> doInBackground(ArrayList<ServerFormDetailsSmap>... values) {  // smap make public
        return multiFormDownloader.downloadForms(values[0], this);
    }

    @Override
    protected void onCancelled(HashMap<ServerFormDetailsSmap, String> formDetailsStringHashMap) {
        synchronized (this) {
            if (stateListener != null) {
                stateListener.formsDownloadingCancelled();
            }
        }
    }

    @Override
    protected void onPostExecute(HashMap<ServerFormDetailsSmap, String> value) {
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

    public void setDownloaderListener(DownloadFormsTaskListenerSmap sl) {
        synchronized (this) {
            stateListener = sl;
        }
    }
}

