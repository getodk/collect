package org.odk.collect.android.formentry;

import android.net.Uri;

import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.tasks.SaveFormToDisk;
import org.odk.collect.android.tasks.SaveToDiskResult;

public class DiskFormSaver implements FormSaver {

    @Override
    public SaveToDiskResult save(FormController formController, boolean shouldFinalize, String updatedSaveName, boolean exitAfter, ProgressListener progressListener, Uri instanceContentURI) {
        SaveFormToDisk saveFormToDisk = new SaveFormToDisk(formController, exitAfter, shouldFinalize, updatedSaveName, instanceContentURI);
        return saveFormToDisk.saveForm(progressListener);
    }
}
