package org.odk.collect.android.location.client;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.location.LocationListener;

import timber.log.Timber;

/**
 * An implementation of {@link LocationClient} that uses the existing
 * Android Location Services (LocationManager) to retrieve the User's
 * location.
 * <p>
 * Should be used whenever there Google Play Services is not present.
 * <p>
 * Package-private, use {@link LocationClients} to retrieve the correct
 * {@link LocationClient}.
 */
class AndroidLocationClient
        extends BaseLocationClient
        implements android.location.LocationListener {

    @Nullable
    private LocationClientListener locationClientListener;

    @Nullable
    private LocationListener locationListener;

    private boolean isConnected;

    /**
     * Constructs a new AndroidLocationClient with the provided Context.
     * This Constructor should be used normally.
     *
     * @param context The Context where the AndroidLocationClient will be running.
     */
    AndroidLocationClient(@NonNull Context context) {
        this((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
    }

    /**
     * Constructs a new AndroidLocationClient with the provided LocationManager.
     * This Constructor should only be used for testing.
     *
     * @param locationManager The LocationManager to retrieve locations from.
     */
    AndroidLocationClient(@NonNull LocationManager locationManager) {
        super(locationManager);
    }

    // LocationClient:

    @Override
    public void start() {
        if (getProvider() == null) {
            if (locationClientListener != null) {
                locationClientListener.onClientStartFailure();
            }

            return;
        }

        isConnected = true;
        if (locationClientListener != null) {
            locationClientListener.onClientStart();
        }
    }

    @Override
    public void stop() {
        // Implementations of LocationClient are expected to call this:
        stopLocationUpdates();
        isConnected = false;

        if (locationClientListener != null) {
            locationClientListener.onClientStop();
        }
    }

    @Override
    public void requestLocationUpdates(@NonNull LocationListener locationListener) {
        if (!isConnected) {
            // This is to maintain expected behavior across LocationClient implementations.
            return;
        }

        if (!isMonitoringLocation()) {
            getLocationManager().requestLocationUpdates(getProvider(), 0, 0, this);
        }

        this.locationListener = locationListener;
    }

    @Override
    public void stopLocationUpdates() {
        if (!isMonitoringLocation()) {
            return;
        }

        getLocationManager().removeUpdates(this);
        this.locationListener = null;
    }

    @Override
    public void setListener(@Nullable LocationClientListener locationClientListener) {
        this.locationClientListener = locationClientListener;
    }

    @Override
    public Location getLastLocation() {
        String provider = getProvider();
        if (provider != null) {
            return getLocationManager().getLastKnownLocation(provider);
        }

        return null;
    }

    @Override
    public boolean isMonitoringLocation() {
        return locationListener != null;
    }

    @Override
    public boolean canSetUpdateIntervals() {
        return false;
    }

    @Override
    public void setUpdateIntervals(long updateInterval, long fastestUpdateInterval) {
        // Do nothing.
        Timber.e("Can't set updateInterval on AndroidLocationClient. You should check canSetUpdateIntervals before calling this method.");
    }

    @Override
    public void resetUpdateIntervals() {
        // Do nothing.
        Timber.e("Can't set updateInterval on AndroidLocationClient. You should check canSetUpdateIntervals before calling this method.");
    }


    // LocationListener:

    @Override
    public void onLocationChanged(Location location) {
        Timber.i("Location changed: %s", location.toString());

        if (locationListener != null) {
            locationListener.onLocationChanged(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
