package org.odk.collect.android.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


/**
 * An implementation of {@link LocationClient} that uses Google Play
 * Services to retrieve the User's location.
 *
 * Should be used whenever there Google Play Services is present.
 *
 * Package-private, use {@link LocationClients} to retrieve the correct
 * {@link LocationClient}.
 */
class GoogleLocationClient implements LocationClient,
        ConnectionCallbacks, OnConnectionFailedListener {

    @NonNull
    private final FusedLocationProviderApi fusedLocationProviderApi;
    @NonNull
    private final GoogleApiClient googleApiClient;

    @Nullable
    private LocationClientListener locationClientListener;

    @Nullable
    private LocationListener locationListener = null;

    private Priority priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY;

    /**
     * Constructs a new GoogleLocationClient with the provided Context.
     *
     * This Constructor should be used normally.
     *
     * @param context The Context where the GoogleLocationClient will be running.
     */
    GoogleLocationClient(@NonNull Context context) {
        this(locationServicesClientForContext(context), LocationServices.FusedLocationApi);
    }

    /**
     * Constructs a new AndroidLocationClient with the provided GoogleApiClient
     * and FusedLocationProviderApi.
     *
     * This Constructor should only be used for testing.
     *
     * @param googleApiClient The GoogleApiClient for managing the LocationClient's connection
     *                        to Play Services.
     *
     * @param fusedLocationProviderApi The FusedLocationProviderApi for fetching the User's
     *                                 location.
     */
    public GoogleLocationClient(@NonNull GoogleApiClient googleApiClient,
                                @NonNull FusedLocationProviderApi fusedLocationProviderApi) {

        this.googleApiClient = googleApiClient;
        this.fusedLocationProviderApi = fusedLocationProviderApi;
    }

    // LocationClient:

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

        fusedLocationProviderApi.requestLocationUpdates(googleApiClient, createLocationRequst(),
                locationListener);

        this.locationListener = locationListener;
    }

    public void stopLocationUpdates() {
        if (this.locationListener == null) {
            return;
        }

        fusedLocationProviderApi.removeLocationUpdates(googleApiClient, this.locationListener);
        this.locationListener = null;
    }

    @Override
    public void setListener(@Nullable LocationClientListener locationClientListener) {
        this.locationClientListener = locationClientListener;
    }

    public void setPriority(@NonNull Priority priority) {
        this.priority = priority;
    }

    @Override
    public Location getLastLocation() {
        // We need to block if the Client isn't already connected:
        if (!googleApiClient.isConnected()) {
            googleApiClient.blockingConnect();
        }

        return fusedLocationProviderApi.getLastLocation(googleApiClient);
    }

    // GoogleLocationClient:

    private LocationRequest createLocationRequst() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(priority.getValue());

        return locationRequest;
    }

    // ConnectionCallbacks:

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (locationClientListener != null) {
            locationClientListener.onStart();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (locationClientListener != null) {
            locationClientListener.onStop();
        }
    }

    // OnConnectionFailedListener:

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (locationClientListener != null) {
            locationClientListener.onStartFailure();
        }
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
