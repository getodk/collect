package org.odk.collect.android.formentry;

import android.net.Uri;

import org.odk.collect.android.tasks.SaveToDiskTaskResult;

public interface FormSaver {
    SaveToDiskTaskResult save(Uri uri, boolean complete, String updatedSaveName, boolean exitAfter, ProgressListener progressListener);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
