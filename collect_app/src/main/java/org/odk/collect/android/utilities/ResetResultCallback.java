package org.odk.collect.android.utilities;

public interface ResetResultCallback {
    void doneResetting();
    void failedToReset(String errorMessage);
}
