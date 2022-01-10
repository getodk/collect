package org.odk.collect.location;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * An interface for classes that allow monitoring and retrieving the User's Location.
 * Currently there are only two implementations:
 * - {@link GoogleFusedLocationClient}: A LocationClient using Google Play Services.
 * - {@link AndroidLocationClient}: A LocationClient using Android's existing Location Services.
 */
public interface LocationClient {
    /**
     * Prepares the LocationClient for use. This method must be called prior
     * to {@link LocationClient#requestLocationUpdates(LocationListener)}
     * or {@link LocationClient#getLastLocation()}.
     */
    void start();

    /**
     * Stops the LocationClient, ending any current connections and allowing
     * resources to be reclaimed.
     * <p>
     * Implementations should call {@link LocationClient#stopLocationUpdates()} if
     * they have been previously requested.
     */
    void stop();

    /**
     * Begins requesting Location updates with the provided {@link LocationListener}
     *
     * @param locationListener The LocationListener to pass location updates to.
     */
    void requestLocationUpdates(@NonNull LocationListener locationListener);

    /**
     * Ends Location updates for the previously provided LocationListener.
     * Implementations should call this from within {@link LocationClient#stop()}.
     */
    void stopLocationUpdates();

    /**
     * Sets the {@link LocationClientListener} which will receive status updates
     * for the LocationClient.  The LocationClient should hold only a WeakReference
     * to the listener so that it does not cause a memory leak.
     *
     * @param locationClientListener The new {@link LocationClientListener}.
     */
    void setListener(@Nullable LocationClientListener locationClientListener);

    /**
     * Sets the LocationClient's {@link Priority} which will be used to determine
     * which Provider (GPS, Network, etc.) will be used to retrieve the User's location.
     * <p>
     * If the LocationClient is already receiving updates, the new Priority will not
     * take effect until the next time Location updates are requested.
     *
     * @param priority The new Priorty.
     */
    void setPriority(@NonNull Priority priority);

    /**
     * Sets whether the client will use the accuracy from mock providers or not. When {@code false}
     * (which it should be by default) the accuracy of {@link Location} objects from mock providers
     * will always be {@code 0}.
     */
    void setRetainMockAccuracy(boolean retainMockAccuracy);

    /**
     * Retrieves the most recent known Location, or null if none is available.
     * This method may block if start was not called before hand.
     *
     * @return The most recent Location.
     */
    @Nullable
    Location getLastLocation();

    /**
     * Provides a way to tell if the LocationClient can access the User's Location based
     * on the current Priority settings. Must be called <b>after</b> Location updates have been requested.
     *
     * @return Whether or not the User's Location is currently available.
     */
    boolean isLocationAvailable();

    /**
     * Provides a way to tell if the LocationClient is currently monitoring the User's Location.
     *
     * @return Whether or not the clientForContext is currently monitoring the User's Location.
     */
    boolean isMonitoringLocation();

    /**
     * Sets the LocationClient's updateInterval (how often we would like updates) and fastestUpdateInterval
     * (for throttling updates that come at a faster interval).
     * <p>
     * Implementations that don't offer this feature should do nothing here.
     *
     * @param updateInterval        How often we would like updates from the LocationClient (inexact).
     * @param fastestUpdateInterval The minimum interval between updates (exact).
     */
    void setUpdateIntervals(long updateInterval, long fastestUpdateInterval);

    /**
     * An interface for listening to status changes on a LocaitonClient.
     */
    interface LocationClientListener {
        /**
         * Called after the LocationClient has been successfully started.
         */
        void onClientStart();

        /**
         * Called if any issue ocurred during LocationClient start-up.
         */
        void onClientStartFailure();

        /**
         * Called after the LocationClient has been stopped, either by calling
         * {@link LocationClient#stop()} or because it was stopped by another process.
         */
        void onClientStop();
    }

    /**
     * Enumerates the options for preferring certain Location Providers over others.
     */
    enum Priority {

        /**
         * Preferred: GPS
         * Backup: Network
         */
        PRIORITY_HIGH_ACCURACY(LocationRequest.PRIORITY_HIGH_ACCURACY),

        /**
         * Preferred: Network
         * Backup: GPS
         */
        PRIORITY_BALANCED_POWER_ACCURACY(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY),

        /**
         * Preferred: Network
         * Backup: GPS (Play Services), Passive (Android)
         */
        PRIORITY_LOW_POWER(LocationRequest.PRIORITY_LOW_POWER),

        /**
         * Preferred: Passive (only receives updates if another Application requests them).
         * Backup: N/A
         */
        PRIORITY_NO_POWER(LocationRequest.PRIORITY_NO_POWER);

        private final int value;

        Priority(int value) {
            this.value = value;
        }

        /**
         * The numeric value of the Priority;
         * LocationServices uses integer constants.
         *
         * @return The integer constant value for the Priority.
         */
        public int getValue() {
            return value;
        }
    }
}
