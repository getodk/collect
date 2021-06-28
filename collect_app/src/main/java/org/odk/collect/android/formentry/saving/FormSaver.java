package org.odk.collect.android.formentry.saving;

import android.net.Uri;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.tasks.SaveToDiskResult;
import org.odk.collect.android.utilities.MediaUtils;

import java.util.ArrayList;

public interface FormSaver {
    SaveToDiskResult save(Uri instanceContentURI, FormController formController, MediaUtils mediaUtils, boolean shouldFinalize, boolean exitAfter,
                          String updatedSaveName, ProgressListener progressListener, Analytics analytics, ArrayList<String> tempFiles, String currentProjectId);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
