package org.odk.collect.android.utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by Divya on 3/2/2017.
 */

public class PlayServicesUtil {
    private static final int PLAY_SERVICE_ERROR_REQUEST_CODE = 1000;
    private static int resultCode;

    private static GoogleApiAvailability googleApiAvailability;

    private PlayServicesUtil() {
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    public static boolean isGooglePlayServicesAvailable(Context context) {
        googleApiAvailability = GoogleApiAvailability.getInstance();
        resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     */
    public static void showGooglePlayServicesAvailabilityErrorDialog(Context context) {
        googleApiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = googleApiAvailability.getErrorDialog(
                (Activity) context,
                resultCode,
                PLAY_SERVICE_ERROR_REQUEST_CODE);
        dialog.show();
    }
}