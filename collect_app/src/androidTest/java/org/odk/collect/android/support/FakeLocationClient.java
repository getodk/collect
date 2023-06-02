package org.odk.collect.android.support;

import android.location.Location;

import androidx.annotation.Nullable;

import com.google.android.gms.location.LocationListener;

import org.odk.collect.location.LocationClient;

public class FakeLocationClient implements LocationClient {

    private LocationClientListener listener;
    private LocationListener locationListener;
    private Location lastLocation;

    public void start() {
        if (getListener() != null) {
            getListener().onClientStart();
        }
    }

    public void stop() {
        stopLocationUpdates();
        if (getListener() != null) {
            getListener().onClientStop();
        }
        setListener(null);
    }

    public boolean isLocationAvailable() {
        return true;
    }

    public void requestLocationUpdates(LocationListener locationListener) {
        this.locationListener = locationListener;
        updateLocationListener();
    }

    public void stopLocationUpdates() {
        this.locationListener = null;
    }

    @Override
    public void setListener(@Nullable LocationClientListener locationClientListener) {
        this.listener = locationClientListener;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public boolean isMonitoringLocation() {
        return locationListener != null;
    }

    public void setPriority(Priority priority) { }

    @Override
    public void setRetainMockAccuracy(boolean retainMockAccuracy) {
        throw new UnsupportedOperationException();
    }

    public void setUpdateIntervals(long updateInterval, long fastestUpdateInterval) { }

    public void setLocation(Location location) {
        this.lastLocation = location;
        updateLocationListener();
    }

    private void updateLocationListener() {
        if (this.locationListener != null && lastLocation != null) {
            this.locationListener.onLocationChanged(lastLocation);
        }
    }

    private LocationClientListener getListener() {
        return listener;
    }
}
