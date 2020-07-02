package odk.hedera.collect.formentry;

import odk.hedera.collect.javarosawrapper.FormController;

public interface RequiresFormController {
    void formLoaded(FormController formController);
}
