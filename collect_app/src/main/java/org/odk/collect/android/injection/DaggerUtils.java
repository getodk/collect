package org.odk.collect.android.injection;

import android.app.Activity;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.config.AppDependencyComponent;

public class DaggerUtils {

    private DaggerUtils() {}

    public static AppDependencyComponent getComponent(Activity activity) {
        return ((Collect) activity.getApplication()).getComponent();
    }
}
