package org.odk.collect.android.support;

import android.app.Activity;

import androidx.test.core.app.ActivityScenario;

import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

/**
 * Allows the use of an {@link androidx.test.core.app.ActivityScenario} like interface when
 * using an Activity that isn't part of the manifest. This is useful when using a mock/fake/stub
 * Activity as part of a test
 */
public final class TestActivityScenario<T extends Activity> {

    private final ActivityController<T> activityController;

    public static <A extends Activity> TestActivityScenario<A> launch(Class<A> activityClass) {
        TestActivityScenario<A> activityScenario = new TestActivityScenario<>(activityClass);
        activityScenario.launchInternal();
        return activityScenario;
    }

    private TestActivityScenario(Class<T> activityClass) {
        activityController = Robolectric.buildActivity(activityClass);
    }

    private void launchInternal() {
        activityController.setup();
    }

    public void onActivity(ActivityScenario.ActivityAction<T> activityAction) {
        activityAction.perform(activityController.get());
    }

    public void recreate() {
        activityController.recreate();
    }
}
