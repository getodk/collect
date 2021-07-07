package org.odk.collect.android.location.client;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.google.android.gms.location.LocationListener;

import org.odk.collect.location.LocationClient;

import timber.log.Timber;

/**
 * Provides location updates for a set timeout period. Once a request is initiated, updates are only
 * provided if the new reading has higher accuracy.
 *
 * New requests reset the timeout and the highest accuracy.
 */
public class MaxAccuracyWithinTimeoutLocationClientWrapper implements LocationClient.LocationClientListener, LocationListener {
    private final LocationClient locationClient;

    private final LocationListener listener;

    /** The highest accuracy reading seen since the current request was started. Null if no updates
     * have been received since the current request was made. **/
    @Nullable
    private Location highestAccuracyReading;
    private final Handler timerHandler;

    private static final LocationClient.Priority DEFAULT_PRIORITY = LocationClient.Priority.PRIORITY_HIGH_ACCURACY;

    public MaxAccuracyWithinTimeoutLocationClientWrapper(LocationClient locationClient, LocationListener listener) {
        this.locationClient = locationClient;
        this.locationClient.setPriority(DEFAULT_PRIORITY);

        this.listener = listener;
        this.timerHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Requests that location updates be provided to the listener for {@code timeoutSeconds}.
     */
    public void requestLocationUpdates(long timeoutSeconds) {
        new Handler(Looper.getMainLooper()).post(() -> {
            locationClient.setListener(this);
            locationClient.start();
        });

        // If updates are requested more than once, reset the highest accuracy
        highestAccuracyReading = null;

        timerHandler.removeCallbacksAndMessages(null);
        timerHandler.postDelayed(() -> {
            locationClient.stop();
            locationClient.setListener(null);
            Timber.i("MaxAccuracyWithinTimeoutLocationClient: stopping location updates");
        }, timeoutSeconds * 1000);
    }

    //region LocationClientListener
    @Override
    public void onClientStart() {
        Timber.i("MaxAccuracyWithinTimeoutLocationClient: starting location updates");
        try {
            locationClient.requestLocationUpdates(this);
        } catch (SecurityException e) {
            // Device-level location permissions have not been granted. The user will be prompted to
            // provide permissions. It will be too late for this triggered action but will work for
            // future ones.
        }
    }

    @Override
    public void onClientStartFailure() {

    }

    @Override
    public void onClientStop() {

    }
    //endregion

    //region LocationListener

    /**
     * Updates the listener if {@code location} meets any of these criteria:
     * - it is the first location update
     * - it is more accurate than the most accurate one seen yet
     * - it is the first location update with accuracy
     *
     * Otherwise, the new location reading is discarded.
     */
    @Override
    public void onLocationChanged(Location location) {
        Timber.i("MaxAccuracyWithinTimeoutLocationClient: got location %s", location);
        if (highestAccuracyReading == null || !highestAccuracyReading.hasAccuracy()
                || location.hasAccuracy() && highestAccuracyReading.hasAccuracy() && location.getAccuracy() < highestAccuracyReading.getAccuracy()) {
            highestAccuracyReading = location;
            listener.onLocationChanged(location);
            Timber.i("MaxAccuracyWithinTimeoutLocationClient: passed on location %s", location);
        }
    }
    //endregion
}
