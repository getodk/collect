package org.odk.collect.android.backgroundwork;

public interface FormUpdateScheduler {

    void scheduleUpdates(String projectId);

    void cancelUpdates(String projectId);
}
