package org.odk.collect.android.application;

import androidx.work.Configuration;
import androidx.work.WorkManager;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;

import static org.robolectric.Shadows.shadowOf;

/**
 * @author James Knight
 *
 * This class will automatically be used by Robolectric
 * tests as a replacement for the application class configured
 * in the Android manifest as it prefixes that class with `Test`.
 */

public class TestCollect extends Collect {

    @Override
    public void onCreate() {
        super.onCreate();

        // Prevents OKHttp from exploding on initialization https://github.com/robolectric/robolectric/issues/5115
        System.setProperty("javax.net.ssl.trustStore", "NONE");

        // We need this so WorkManager.getInstance doesn't explode
        try {
            WorkManager.initialize(RuntimeEnvironment.application, new Configuration.Builder().build());
        } catch (IllegalStateException e) {
            // initialize() explodes if it's already been called
        }

        // We don't want to deal with permission checks in Robolectric
        ShadowApplication shadowApplication = shadowOf(this);
        shadowApplication.grantPermissions("android.permission.ACCESS_FINE_LOCATION");
        shadowApplication.grantPermissions("android.permission.ACCESS_COARSE_LOCATION");
        shadowApplication.grantPermissions("android.permission.READ_EXTERNAL_STORAGE");
        shadowApplication.grantPermissions("android.permission.WRITE_EXTERNAL_STORAGE");
        shadowApplication.grantPermissions("android.permission.CAMERA");
        shadowApplication.grantPermissions("android.permission.READ_PHONE_STATE");
        shadowApplication.grantPermissions("android.permission.RECORD_AUDIO");
        shadowApplication.grantPermissions("android.permission.GET_ACCOUNTS");
    }
}