package org.odk.collect.android.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Retrieves the User's location via Google Play Services.
 */
public class LocationClient implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @NonNull
    private final FusedLocationProviderApi fusedLocationProviderApi;
    @NonNull
    private final GoogleApiClient googleApiClient;

    @Nullable
    private LocationListener locationListener = null;

    private int priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

    /**
     * For normal use.
     * @param context The Context the LocationClient will be used with.
     */
    public LocationClient(@NonNull Context context) {
        this(locationServicesClientForContext(context), LocationServices.FusedLocationApi);
    }

    /**
     * For testing.
     * @param googleApiClient
     * @param fusedLocationProviderApi
     */
    public LocationClient(@NonNull GoogleApiClient googleApiClient, @NonNull FusedLocationProviderApi fusedLocationProviderApi) {
        this.googleApiClient = googleApiClient;
        this.fusedLocationProviderApi = fusedLocationProviderApi;
    }

    public void start() {
        googleApiClient.registerConnectionCallbacks(this);
        googleApiClient.registerConnectionFailedListener(this);

        googleApiClient.connect();
    }

    public void stop() {
        stopLocationUpdates();

        googleApiClient.unregisterConnectionCallbacks(this);
        googleApiClient.unregisterConnectionFailedListener(this);

        googleApiClient.disconnect();
    }

    public void requestLocationUpdates(@NonNull LocationListener locationListener) {
        if (this.locationListener != null) {
            stopLocationUpdates();
        }

        fusedLocationProviderApi.requestLocationUpdates(googleApiClient, createLocationRequst(), locationListener);
        this.locationListener = locationListener;
    }

    public void stopLocationUpdates() {
        if (this.locationListener == null) {
            return;
        }

        fusedLocationProviderApi.removeLocationUpdates(googleApiClient, this.locationListener);
        this.locationListener = null;
    }

    public void setPriority(int priority) {
        switch (priority) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
            case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
            case LocationRequest.PRIORITY_LOW_POWER:
            case LocationRequest.PRIORITY_NO_POWER:
                this.priority = priority;

            default:
                throw new IllegalArgumentException("Priority must be one of the LocationRequest constants.");
        }
    }

    private LocationRequest createLocationRequst() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(priority);

        return locationRequest;
    }

    // ConnectionCallbacks:

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // TODO: Should we Log something here? Pass up to Activity?
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // TODO: Should we Log something here? Pass up to Activity?
    }

    // OnConnectionFailedListener:

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // TODO: Should we Log something here? Pass up to Activity?
    }

    /**
     * Helper method for building a GoogleApiClient with the LocationServices API.
     * @param context The Context for building the GoogleApiClient.
     * @return A GoogleApiClient with the LocationServices API.
     */
    private static GoogleApiClient locationServicesClientForContext(@NonNull Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build();
    }
}
