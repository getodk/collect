package org.odk.collect.android.location.client;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GoogleApiAvailability;

/**
 * A helper class for getting a LocationClient based on whether or not Google Play Services are
 * available on the device.
 */
public class LocationClients {

    private LocationClients() {

    }

    @Nullable
    private static LocationClient testClient;

    /**
     * Checks and returns a {@link LocationClient} based on whether or not Google Play Services
     * are available.
     *
     * 3/14/2018 - GoogleLocationClient removed because of user reports that accuracy does not get
     * better than 10m.
     *
     * @param context The Context the LocationClient will be used within.
     * @return An implementation of LocationClient.
     */
    public static LocationClient clientForContext(@NonNull Context context) {

        return testClient != null
                ? testClient
                : new AndroidLocationClient(context);
    }

    /**
     * For testing purposes only. A poorman's Dependency Injection.
     * @param testClient The test or mock LocationClient to use.
     */
    public static void setTestClient(@NonNull LocationClient testClient) {
        LocationClients.testClient = testClient;
    }

    //    private static boolean areGooglePlayServicesAvailable(@NonNull Context context) {
    //        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
    //        int status = googleApiAvailability.isGooglePlayServicesAvailable(context);
    //
    //        return status == ConnectionResult.SUCCESS;
    //    }
}