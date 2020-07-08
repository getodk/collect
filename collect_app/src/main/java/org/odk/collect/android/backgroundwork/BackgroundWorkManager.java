package org.odk.collect.android.backgroundwork;

public interface BackgroundWorkManager {

    boolean isFormUploaderRunning();

    boolean isFormDownloaderRunning();

    void scheduleMatchExactlySync();

    void cancelMatchExactlySync();
}
