package org.odk.collect.android;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerTestComponent;
import org.odk.collect.android.injection.TestComponent;
import org.robolectric.RuntimeEnvironment;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class DaggerTest {

    protected TestComponent testComponent;

    protected abstract void injectDependencies();

    @OverridingMethodsMustInvokeSuper
    public void setUp() {
        testComponent = DaggerTestComponent.builder().application(RuntimeEnvironment.application).build();
        ((Collect) RuntimeEnvironment.application).setComponent(testComponent);

        injectDependencies();
    }
}
