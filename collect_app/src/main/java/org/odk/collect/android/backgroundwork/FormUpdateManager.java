package org.odk.collect.android.backgroundwork;

public interface FormUpdateManager {

    void scheduleUpdates();

    boolean isFormUploaderRunning();

    boolean isUpdateRunning();
}
