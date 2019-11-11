package org.odk.collect.android.location.client;

import android.content.Context;

import org.odk.collect.android.utilities.PlayServicesUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** A static helper class for obtaining the appropriate LocationClient to use. */
public class LocationClientProvider {
    @Nullable private static LocationClient testClient;

    private LocationClientProvider() { }  // prevent instantiation of this utility class

    /** Returns a LocationClient appropriate for a given context. */
    // NOTE(ping): As of 2018-11-01, the GoogleFusedLocationClient never returns an
    // accuracy radius below 3m: https://issuetracker.google.com/issues/118789585
    public static LocationClient getClient(@NonNull Context context) {
        return testClient != null
            ? testClient
            : PlayServicesUtil.isGooglePlayServicesAvailable(context)
                ? new GoogleFusedLocationClient(context)
                : new AndroidLocationClient(context);
    }

    /** Sets the LocationClient.  For use in tests only. */
    public static void setTestClient(@NonNull LocationClient testClient) {
        LocationClientProvider.testClient = testClient;
    }
}
