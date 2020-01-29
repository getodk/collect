package org.odk.collect.android.formentry;

import android.net.Uri;

import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.tasks.SaveToDiskResult;

public interface FormSaver {
    SaveToDiskResult save(FormController formController, boolean shouldFinalize, String updatedSaveName, boolean exitAfter, ProgressListener progressListener, Uri instanceContentURI);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
