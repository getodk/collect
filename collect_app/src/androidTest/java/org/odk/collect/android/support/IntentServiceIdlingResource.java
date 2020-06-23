package org.odk.collect.android.support;

import android.app.ActivityManager;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingResource;

public class IntentServiceIdlingResource implements IdlingResource {

    private ResourceCallback resourceCallback;
    private final String serviceName;

    public IntentServiceIdlingResource(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String getName() {
        return serviceName;
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = !isMigrationRunning();
        if (idle && resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        }
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

    private boolean isMigrationRunning() {
        ActivityManager manager = (ActivityManager) ApplicationProvider.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (info.service.getClassName().equals(serviceName)) {
                return true;
            }
        }

        return false;
    }
}
