package org.odk.collect.android.widgets.utilities;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.javarosawrapper.FormController;

import timber.log.Timber;

public class FormControllerWaitingForDataRegistry implements WaitingForDataRegistry {

    @Override
    public void waitForData(FormIndex index) {
        Collect collect = Collect.getInstance();
        if (collect == null) {
            throw new IllegalStateException("Collect application instance is null.");
        }

        FormController formController = collect.getFormController();
        if (formController == null) {
            Timber.w("Can not call setIndexWaitingForData() because of null formController");
            return;
        }

        formController.setIndexWaitingForData(index);
    }

    @Override
    public boolean isWaitingForData(FormIndex index) {
        Collect collect = Collect.getInstance();
        if (collect == null) {
            throw new IllegalStateException("Collect application instance is null.");
        }

        FormController formController = collect.getFormController();
        if (formController == null) {
            return false;
        }

        return index.equals(formController.getIndexWaitingForData());
    }

    @Override
    public void cancelWaitingForData() {
        Collect collect = Collect.getInstance();
        if (collect == null) {
            throw new IllegalStateException("Collect application instance is null.");
        }

        FormController formController = collect.getFormController();
        if (formController == null) {
            return;
        }

        formController.setIndexWaitingForData(null);
    }
}
