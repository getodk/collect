package org.odk.collect.android.backgroundwork;

public interface InstanceSubmitScheduler {

    void scheduleAutoSend(String projectId);

    void scheduleFormAutoSend(String projectId);

    void cancelSubmit(String projectId);
}
