package org.odk.collect.android.location.client;

import android.content.Context;

import org.odk.collect.android.utilities.PlayServicesUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A helper class for getting a LocationClient based on whether or not Google Play Services are
 * available on the device.
 */
public class LocationClients {
    @Nullable private static LocationClient testClient = null;

    /** Returns a {@link LocationClient} appropriate for a given context. */
    // NOTE(ping): As of 2018-11-01, the GoogleLocationClient never returns an
    // accuracy radius below 3m: https://issuetracker.google.com/issues/118789585
    public static LocationClient clientForContext(@NonNull Context context) {
        return testClient != null
            ? testClient
            : PlayServicesUtil.isGooglePlayServicesAvailable(context)
                ? new GoogleLocationClient(context)
                : new AndroidLocationClient(context);
    }

    /** Sets the LocationClient.  For use in tests only. */
    public static void setTestClient(@NonNull LocationClient testClient) {
        LocationClients.testClient = testClient;
    }
}
