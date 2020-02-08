package org.odk.collect.android.formentry;

import android.net.Uri;

import org.odk.collect.android.tasks.SaveToDiskResult;

public interface FormSaver {
    SaveToDiskResult save(Uri instanceContentURI, boolean shouldFinalize, String updatedSaveName, boolean exitAfter, ProgressListener progressListener);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
