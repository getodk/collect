package org.odk.collect.android.formentry;

import androidx.annotation.Nullable;

import org.odk.collect.android.javarosawrapper.FormController;

public interface FormControllerProvider {

    @Nullable
    FormController getFormController();
}
