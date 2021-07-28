package org.odk.collect.android.application;

import androidx.test.core.app.ApplicationProvider;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import org.odk.collect.android.database.DatabaseConnection;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowEnvironment;

import static android.os.Environment.MEDIA_MOUNTED;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author James Knight
 */

public class RobolectricApplication extends Collect {

    @Override
    public void onCreate() {
        // Make sure storage is accessible
        ShadowEnvironment.setExternalStorageState(MEDIA_MOUNTED);

        // Prevents OKHttp from exploding on initialization https://github.com/robolectric/robolectric/issues/5115
        System.setProperty("javax.net.ssl.trustStore", "NONE");

        // We need this so WorkManager.getInstance doesn't explode
        try {
            WorkManager.initialize(ApplicationProvider.getApplicationContext(), new Configuration.Builder().build());
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
        DatabaseConnection.closeAll();

        // We don't want any clicks to be blocked
        MultiClickGuard.test = true;

        super.onCreate();
    }
}
