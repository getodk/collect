package odk.hedera.collect.formentry.saving;

import android.net.Uri;

import odk.hedera.collect.analytics.Analytics;
import odk.hedera.collect.javarosawrapper.FormController;
import odk.hedera.collect.tasks.SaveToDiskResult;

public interface FormSaver {
    SaveToDiskResult save(Uri instanceContentURI, FormController formController, boolean shouldFinalize, boolean exitAfter, String updatedSaveName, ProgressListener progressListener, Analytics analytics);

    interface ProgressListener {
        void onProgressUpdate(String message);
    }
}
