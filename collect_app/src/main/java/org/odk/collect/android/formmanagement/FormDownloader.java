package org.odk.collect.android.formmanagement;

import java.util.function.Supplier;

import javax.annotation.Nullable;

public interface FormDownloader {

    void downloadForm(ServerFormDetails form, @Nullable ProgressReporter progressReporter, @Nullable Supplier<Boolean> isCancelled) throws FormDownloadException, InterruptedException;

    interface ProgressReporter {
        void onDownloadingMediaFile(int count);
    }
}
