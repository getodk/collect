package org.odk.collect.android.support;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.logic.FormController;

public final class CollectHelpers {

    private CollectHelpers() {}

    public static FormController waitForFormController() throws InterruptedException {
        if (Collect.getInstance().getFormController() == null) {
            do {
                Thread.sleep(1);
            } while (Collect.getInstance().getFormController() == null);
        }

        return Collect.getInstance().getFormController();
    }

    public static AppDependencyComponent getAppDependencyComponent() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Collect application = (Collect) context;
        return application.getComponent();
    }
}
