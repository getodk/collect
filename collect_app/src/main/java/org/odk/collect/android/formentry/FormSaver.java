package org.odk.collect.android.formentry;

import android.net.Uri;

import org.odk.collect.android.tasks.SaveToDiskResult;

public interface FormSaver {
    SaveToDiskResult save(Uri uri, boolean shouldFinalize, String updatedSaveName, boolean exitAfter, ProgressListener progressListener);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
