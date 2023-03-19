package org.odk.collect.android.widgets.utilities;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.javarosawrapper.FormController;

import java.util.function.Supplier;

import timber.log.Timber;

public class FormControllerWaitingForDataRegistry implements WaitingForDataRegistry {

    private final Supplier<FormController> formControllerProvider;

    public FormControllerWaitingForDataRegistry(Supplier<FormController> formControllerProvider) {
        this.formControllerProvider = formControllerProvider;
    }

    @Override
    public void waitForData(FormIndex index) {
        FormController formController = formControllerProvider.get();
        if (formController == null) {
            Timber.w("Can not call setIndexWaitingForData() because of null formController");
            return;
        }

        formController.setIndexWaitingForData(index);
    }

    @Override
    public boolean isWaitingForData(FormIndex index) {
        FormController formController = formControllerProvider.get();
        if (formController == null) {
            return false;
        }

        return index.equals(formController.getIndexWaitingForData());
    }

    @Override
    public void cancelWaitingForData() {
        FormController formController = formControllerProvider.get();
        if (formController == null) {
            return;
        }

        formController.setIndexWaitingForData(null);
    }
}
