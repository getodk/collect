package org.odk.collect.android.formentry;

import org.odk.collect.android.javarosawrapper.FormController;

public interface RequiresFormController {
    void formLoaded(FormController formController);
}
