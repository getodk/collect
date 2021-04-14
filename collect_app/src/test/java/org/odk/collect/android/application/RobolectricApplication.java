package org.odk.collect.android.application;

import androidx.work.Configuration;
import androidx.work.WorkManager;

import org.odk.collect.android.database.FormsDatabaseProvider;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;

import javax.inject.Inject;

import static org.robolectric.Shadows.shadowOf;

/**
 * @author James Knight
 */

public class RobolectricApplication extends Collect {

    @Inject
    FormsDatabaseProvider formsDatabaseProvider;

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
        shadowApplication.grantPermissions("android.permission.CAMERA");
        shadowApplication.grantPermissions("android.permission.READ_PHONE_STATE");
        shadowApplication.grantPermissions("android.permission.RECORD_AUDIO");
        shadowApplication.grantPermissions("android.permission.GET_ACCOUNTS");

        // These clear static state that can't persist from test to test
        DaggerUtils.getComponent(this).formsDatabaseProvider().releaseDatabaseHelper();
        DaggerUtils.getComponent(this).instancesDatabaseProvider().releaseDatabaseHelper();

        // We don't want any clicks to be blocked
        MultiClickGuard.test = true;
    }
}
