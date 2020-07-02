package odk.hedera.collect.formentry.saving;

import android.net.Uri;

import odk.hedera.collect.analytics.Analytics;
import odk.hedera.collect.javarosawrapper.FormController;
import odk.hedera.collect.tasks.SaveFormToDisk;
import odk.hedera.collect.tasks.SaveToDiskResult;

public class DiskFormSaver implements FormSaver {

    @Override
    public SaveToDiskResult save(Uri instanceContentURI, FormController formController, boolean shouldFinalize, boolean exitAfter, String updatedSaveName, ProgressListener progressListener, Analytics analytics) {
        SaveFormToDisk saveFormToDisk = new SaveFormToDisk(formController, exitAfter, shouldFinalize, updatedSaveName, instanceContentURI, analytics);
        return saveFormToDisk.saveForm(progressListener);
    }
}
