package org.odk.collect.android.utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.odk.collect.android.tasks.GoogleSheetsTask;

/**
 * Created by Divya on 3/2/2017.
 */

public class PlayServicesUtil {

    public static int PLAY_SERVICE_ERROR_REQUEST_CODE = 1001;

    private static GoogleApiAvailability googleApiAvailability;
    private static int resultCode;

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

    public static void requestPlayServicesErrorDialog(Context context) {
        if (googleApiAvailability.isUserResolvableError(resultCode)) {
            googleApiAvailability.getErrorDialog((Activity) context,
                    resultCode, PLAY_SERVICE_ERROR_REQUEST_CODE)
                    .show();
        } else {
            ((Activity) context).finish();
        }
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    public static void acquireGooglePlayServices(Activity activityContext) {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(activityContext);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode, activityContext);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     * @param activityContext
     */
    private static void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode, Activity activityContext) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                activityContext,
                connectionStatusCode,
                GoogleSheetsTask.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

}
