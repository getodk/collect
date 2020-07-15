package org.odk.collect.android.backgroundwork;

public interface BackgroundWorkManager {

    boolean isFormUploaderRunning();

    boolean isFormDownloaderRunning();

    void scheduleMatchExactlySync(long repeatPeriod);

    void cancelMatchExactlySync();

    void scheduleAutoUpdate(long repeatPeriod);
}
