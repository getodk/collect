package org.odk.collect.android.listeners;

import org.javarosa.core.model.FormIndex;

public interface FormActivityListener {

    void createConstraintToast(FormIndex index, int status);

    void createErrorDialog(String message, boolean shouldExit);
}
