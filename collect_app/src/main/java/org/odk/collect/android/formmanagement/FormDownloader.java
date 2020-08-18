package org.odk.collect.android.formmanagement;

import javax.annotation.Nullable;

public interface FormDownloader {

    void downloadForm(ServerFormDetails form, @Nullable ProgressReporter progressReporter) throws FormDownloadException;

    interface ProgressReporter {
        void onDownloadingMediaFile(int count);
    }
}
