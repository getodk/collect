package org.odk.collect.android.utilities;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by Divya on 3/2/2017.
 */

public class PlayServicesUtil {

    public static int PLAY_SERVICE_ERROR_REQUEST_CODE = 1001;

    public static boolean checkPlayServices(Context context) {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog((Activity) context, resultCode, PLAY_SERVICE_ERROR_REQUEST_CODE)
                        .show();
            } else {
                ((Activity) context).finish();
            }
            return false;
        }
        return true;
    }
}
