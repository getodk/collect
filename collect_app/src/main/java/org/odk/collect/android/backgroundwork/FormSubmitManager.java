package org.odk.collect.android.backgroundwork;

public interface FormSubmitManager {

    boolean isSubmitRunning();

    void scheduleSubmit();
}
