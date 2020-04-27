package org.odk.collect.android.formentry.saving;

import android.net.Uri;

import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.tasks.SaveToDiskResult;

public interface FormSaver {
    SaveToDiskResult save(Uri instanceContentURI, FormController formController, boolean shouldFinalize, boolean exitAfter, String updatedSaveName, ProgressListener progressListener,
                          long taskId, String formPath, String surveyNotes, boolean canUpdate, boolean saveMessage);  // smap added task, formPath, surveyNotes, canUpdate, saveMessage

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
