package org.odk.collect.android;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.AndroidTestComponent;
import org.odk.collect.android.injection.DaggerAndroidTestComponent;

public abstract class DaggerAndroidTest {

    public DaggerAndroidTest() {
        AndroidTestComponent androidTestComponent = DaggerAndroidTestComponent.builder().application(Collect.getInstance()).build();
        Collect.getInstance().setComponent(androidTestComponent);
        injectDependencies(androidTestComponent);
    }

    protected abstract void injectDependencies(AndroidTestComponent androidTestComponent);
}
