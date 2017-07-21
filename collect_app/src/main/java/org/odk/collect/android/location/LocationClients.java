package org.odk.collect.android.location;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * A helper class for getting a LocationClient based on whether or not Google Play Services are
 * available on the device.
 */
public class LocationClients {
    /**
     * Checks and returns a {@link LocationClient} based on whether or not Google Play Services
     * are available.
     *
     * @param context The Context the LocationClient will be used within.
     * @return An implementation of LocationClient.
     */
    public static LocationClient client(@NonNull Context context) {
        return areGooglePlayServicesAvailable(context)
                ? new GoogleLocationClient(context)
                : new AndroidLocationClient(context);
    }

    private static boolean areGooglePlayServicesAvailable(@NonNull Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(context);

        return status == ConnectionResult.SUCCESS;
    }
}
