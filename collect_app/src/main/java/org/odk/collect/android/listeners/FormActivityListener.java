package org.odk.collect.android.listeners;

import android.net.Uri;

import org.javarosa.core.model.FormIndex;

public interface FormActivityListener {

    void createConstraintToast(FormIndex index, int status);

    void createErrorDialog(String message, boolean shouldExit);

    void saveChosenImage(Uri uri);

    void saveChosenFile(Uri uri);
}
