package org.odk.collect.location;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import timber.log.Timber;

/**
 * An implementation of {@link LocationClient} that uses Google Play Services to retrieve the
 * User's location.
 * <p>
 * Should be used whenever there Google Play Services is present. In general, use
 * {@link LocationClientProvider} to retrieve a configured {@link LocationClient}.
 */
@SuppressLint("MissingPermission") // Permission checks for location services handled in components that use this class
public class GoogleFusedLocationClient
        extends BaseLocationClient
        implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    /**
     * The default requested time between location updates, in milliseconds.
     */
    private static final long DEFAULT_UPDATE_INTERVAL = 5000;

    /**
     * The default maximum rate at which location updates can arrive (other updates will be throttled),
     * in milliseconds.
     */
    private static final long DEFAULT_FASTEST_UPDATE_INTERVAL = 2500;

    /**
     * Although FusedLocationProviderApi is deprecated, FusedLocationProviderClient which is
     * supposed to replace it doesn't work until Google Play Services 11.6.0, released Nov 2017.
     * Some of our users have really slow connections and old versions of Play Services so we should
     * wait to switch APIs.
     */
    @NonNull
    private final FusedLocationProviderApi fusedLocationProviderApi;

    @NonNull
    private final GoogleApiClient googleApiClient;

    @Nullable
    private LocationListener locationListener;

    private long updateInterval = DEFAULT_UPDATE_INTERVAL;
    private long fastestUpdateInterval = DEFAULT_FASTEST_UPDATE_INTERVAL;

    /**
     * Constructs a new GoogleFusedLocationClient with the provided Context.
     * <p>
     * This Constructor should be used normally.
     *
     * @param application The application. Used as the Context for building the GoogleApiClient because
     *                    it doesn't release context.
     */
    public GoogleFusedLocationClient(@NonNull Application application) {
        this(locationServicesClientForContext(application), LocationServices.FusedLocationApi,
                (LocationManager) application.getSystemService(Context.LOCATION_SERVICE));
    }

    /**
     * Constructs a new AndroidLocationClient with the provided GoogleApiClient
     * and FusedLocationProviderApi.
     * <p>
     * This Constructor should only be used for testing.
     *
     * @param googleApiClient          The GoogleApiClient for managing the LocationClient's connection
     *                                 to Play Services.
     * @param fusedLocationProviderApi The FusedLocationProviderApi for fetching the User's
     *                                 location.
     */
    public GoogleFusedLocationClient(@NonNull GoogleApiClient googleApiClient,
                                     @NonNull FusedLocationProviderApi fusedLocationProviderApi,
                                     @NonNull LocationManager locationManager) {
        super(locationManager);

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

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        if (getListener() != null) {
            getListener().onClientStop();
        }
    }

    public void requestLocationUpdates(@NonNull LocationListener locationListener) {
        if (!isMonitoringLocation() && googleApiClient.isConnected()) {
            fusedLocationProviderApi.requestLocationUpdates(googleApiClient, createLocationRequest(), this);
        }

        this.locationListener = locationListener;
    }

    public void stopLocationUpdates() {
        if (!isMonitoringLocation()) {
            return;
        }

        if (googleApiClient.isConnected()) {
            fusedLocationProviderApi.removeLocationUpdates(googleApiClient, locationListener);
        }
        locationListener = null;
    }

    @Override
    public Location getLastLocation() {
        // We need to block if the Client isn't already connected:
        if (!googleApiClient.isConnected()) {
            googleApiClient.blockingConnect();
        }

        return LocationUtils.sanitizeAccuracy(fusedLocationProviderApi.getLastLocation(googleApiClient));
    }

    @Override
    public boolean isMonitoringLocation() {
        return locationListener != null;
    }

    @Override
    public boolean canSetUpdateIntervals() {
        return true;
    }

    @Override
    public void setUpdateIntervals(long updateInterval, long fastestUpdateInterval) {
        Timber.i("GoogleFusedLocationClient setting update intervals: %d, %d", updateInterval, fastestUpdateInterval);

        this.updateInterval = updateInterval;
        this.fastestUpdateInterval = fastestUpdateInterval;
    }

    // GoogleFusedLocationClient:

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(getPriority().getValue());

        locationRequest.setInterval(updateInterval);
        locationRequest.setFastestInterval(fastestUpdateInterval);

        return locationRequest;
    }

    // ConnectionCallbacks:

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (getListener() != null) {
            getListener().onClientStart();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    // OnConnectionFailedListener:

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (getListener() != null) {
            getListener().onClientStartFailure();
        }
    }

    // LocationListener:

    @Override
    public void onLocationChanged(Location location) {
        Timber.i("Location changed: %s", location.toString());

        if (locationListener != null) {
            locationListener.onLocationChanged(LocationUtils.sanitizeAccuracy(location));
        }
    }

    /**
     * Helper method for building a GoogleApiClient with the LocationServices API.
     *
     * @param context The Context for building the GoogleApiClient.
     * @return A GoogleApiClient with the LocationServices API.
     */
    private static GoogleApiClient locationServicesClientForContext(@NonNull Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build();
    }
}
