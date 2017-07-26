package org.odk.collect.android.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.location.LocationListener;

import java.util.List;

/**
 * An implementation of {@link LocationClient} that uses the existing
 * Android Location Services (LocationManager) to retrieve the User's
 * location.
 *
 * Should be used whenever there Google Play Services is not present.
 *
 * Package-private, use {@link LocationClients} to retrieve the correct
 * {@link LocationClient}.
 */
public class AndroidLocationClient implements LocationClient,
        android.location.LocationListener {

    @NonNull
    private final LocationManager locationManager;

    @Nullable
    private LocationClientListener locationClientListener;

    @Nullable
    private LocationListener locationListener;

    @NonNull
    private Priority priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY;

    private Location cachedLocation;
    private boolean isConnected;

    /**
     * Constructs a new AndroidLocationClient with the provided Context.
     * This Constructor should be used normally.
     * @param context The Context where the AndroidLocationClient will be running.
     */
    public AndroidLocationClient(@NonNull Context context) {
        this((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
    }

    /**
     * Constructs a new AndroidLocationClient with the provided LocationManager.
     * This Constructor should only be used for testing.
     * @param locationManager The LocationManager to retrieve locations from.
     */
    AndroidLocationClient(@NonNull LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    // LocationClient:

    @Override
    public void start() {
        if (getProvider() == null) {
            if (locationClientListener != null) {
                locationClientListener.onStartFailure();
            }

            return;
        }

        isConnected = true;
        if (locationClientListener != null) {
            locationClientListener.onStart();
        }
    }

    @Override
    public void stop() {
        // Implementations of LocationClient are expected to call this:
        stopLocationUpdates();
        isConnected = false;

        if (locationClientListener != null) {
            locationClientListener.onStop();
        }
    }

    @Override
    public void requestLocationUpdates(@NonNull LocationListener locationListener) {
        if (!isConnected) {
            // This is to maintain expected behavior across LocationClient implementations.
            return;
        }

        if (!isMonitoringLocation()) {
            locationManager.requestLocationUpdates(getProvider(), 0, 0, this);
        }

        this.locationListener = locationListener;
    }

    @Override
    public void stopLocationUpdates() {
        if (!isMonitoringLocation()) {
            return;
        }

        locationManager.removeUpdates(this);
        this.locationListener = null;
    }

    @Override
    public void setListener(@Nullable LocationClientListener locationClientListener) {
        this.locationClientListener = locationClientListener;
    }

    @Override
    public void setPriority(@NonNull Priority priority) {
        this.priority = priority;
    }

    @Override
    public Location getLastLocation() {
        String provider = getProvider();
        if (provider != null) {
            return locationManager.getLastKnownLocation(provider);
        }

        return null;
    }

    @Override
    public boolean isMonitoringLocation() {
        return locationListener != null;
    }

    // AndroidLocationClient:

    private String getProvider() {
        String provider = LocationManager.PASSIVE_PROVIDER;
        String backupProvider = null;

        switch (priority) {

            case PRIORITY_HIGH_ACCURACY:
                provider = LocationManager.GPS_PROVIDER;
                backupProvider = LocationManager.NETWORK_PROVIDER;
                break;

            case PRIORITY_BALANCED_POWER_ACCURACY:
                provider = LocationManager.NETWORK_PROVIDER;
                backupProvider = LocationManager.GPS_PROVIDER;
                break;

            case PRIORITY_LOW_POWER:
                provider = LocationManager.NETWORK_PROVIDER;
                backupProvider = LocationManager.PASSIVE_PROVIDER;
                break;

            case PRIORITY_NO_POWER:
                provider = LocationManager.PASSIVE_PROVIDER;
                backupProvider = null;
                break;
        }

        return getProviderIfEnabled(provider, backupProvider);
    }

    @Nullable
    private String getProviderIfEnabled(@NonNull String provider, @Nullable String backupProvider) {
        if (hasProvider(provider)) {
            return provider;

        } else if (hasProvider(backupProvider)) {
            return backupProvider;
        }

        return null;
    }

    private boolean hasProvider(@Nullable String provider) {
        if (provider == null) {
            return false;
        }

        List<String> enabledProviders = locationManager.getProviders(true);
        for (String enabledProvider : enabledProviders) {
            if (enabledProvider.equalsIgnoreCase(provider)) {
                return true;
            }
        }

        return false;
    }

    // LocationListener:

    @Override
    public void onLocationChanged(Location location) {
        if (locationListener != null) {
            locationListener.onLocationChanged(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
