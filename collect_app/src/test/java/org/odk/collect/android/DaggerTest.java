package org.odk.collect.android;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerTestComponent;
import org.odk.collect.android.injection.TestComponent;
import org.robolectric.RuntimeEnvironment;

public abstract class DaggerTest {

    public DaggerTest() {
        TestComponent testComponent = DaggerTestComponent.builder().application(RuntimeEnvironment.application).build();
        ((Collect) RuntimeEnvironment.application).setComponent(testComponent);
        injectDependencies(testComponent);
    }

    protected abstract void injectDependencies(TestComponent testComponent);
}
