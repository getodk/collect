package org.odk.collect.android;

import android.app.Application;

import org.junit.runners.model.InitializationError;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

/**
 * @author James Knight
 */

public class CollectTestRunner extends RobolectricTestRunner {
    public CollectTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Nonnull
    @Override
    protected Class<? extends TestLifecycle> getTestLifecycleClass() {
        return CollectTestLifecycle.class;
    }

    public static class CollectTestLifecycle extends DefaultTestLifecycle {
        @Override
        public Application createApplication(Method method, AndroidManifest appManifest, Config config) {
            return new TestCollect();
        }
    }
}
