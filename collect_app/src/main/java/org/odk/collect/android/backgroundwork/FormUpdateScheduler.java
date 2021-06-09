package org.odk.collect.android.backgroundwork;

public interface FormUpdateScheduler {

    void scheduleUpdates(String s);

    void cancelUpdates(String s);
}
