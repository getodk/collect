package org.odk.collect.android.formentry;

import android.net.Uri;

import org.odk.collect.android.tasks.SaveFormToDisk;
import org.odk.collect.android.tasks.SaveToDiskResult;

public class DiskFormSaver implements FormSaver {

    @Override
    public SaveToDiskResult save(Uri uri, boolean shouldFinalize, String updatedSaveName, boolean exitAfter, ProgressListener progressListener) {
        SaveFormToDisk saveFormToDisk = new SaveFormToDisk(uri, shouldFinalize, exitAfter, updatedSaveName);
        return saveFormToDisk.saveForm(progressListener);
    }
}
