package org.odk.collect.android.formentry.saving;

import android.net.Uri;

import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.tasks.SaveFormToDisk;
import org.odk.collect.android.tasks.SaveToDiskResult;

public class DiskFormSaver implements FormSaver {

    @Override
    public SaveToDiskResult save(Uri instanceContentURI, FormController formController, boolean shouldFinalize, boolean exitAfter, String updatedSaveName, ProgressListener progressListener,
                                 long taskId, String formPath, String surveyNotes, boolean canUpdate, boolean saveMessage) {		// smap added task, formPath, surveyNotes, canUpdate, saveMessage
        SaveFormToDisk saveFormToDisk = new SaveFormToDisk(formController, exitAfter, shouldFinalize, updatedSaveName, instanceContentURI,
                    taskId, formPath, surveyNotes, canUpdate, saveMessage);		// smap added task, formPath, surveyNotes, canUpdate, saveMessage
        return saveFormToDisk.saveForm(progressListener);
    }
}
