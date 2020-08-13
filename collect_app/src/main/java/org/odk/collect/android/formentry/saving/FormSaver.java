package org.odk.collect.android.formentry.saving;

import android.net.Uri;

import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.tasks.SaveToDiskResult;
import org.odk.collect.android.utilities.MediaManager;

public interface FormSaver {
    SaveToDiskResult save(Uri instanceContentURI, FormController formController, boolean shouldFinalize, boolean exitAfter,
                          String updatedSaveName, ProgressListener progressListener, MediaManager mediaManager, Analytics analytics);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
