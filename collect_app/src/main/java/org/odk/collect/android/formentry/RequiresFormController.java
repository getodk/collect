package org.odk.collect.android.formentry;

import androidx.annotation.NonNull;

import org.odk.collect.android.javarosawrapper.FormController;

public interface RequiresFormController {
    void formLoaded(@NonNull FormController formController);
}
