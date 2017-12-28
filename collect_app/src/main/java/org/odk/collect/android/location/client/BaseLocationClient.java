package org.odk.collect.android.location.client;

import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;


/**
 * An abstract base LocationClient class that provides some shared functionality for determining
 * whether or not certain Location providers are available.
 */
abstract class BaseLocationClient implements LocationClient {

    @NonNull
    private final LocationManager locationManager;

    @NonNull
    private Priority priority = Priority.PRIORITY_HIGH_ACCURACY;

    /**
     * Constructs a new BaseLocationClient with the provided LocationManager.
     * This Constructor is only accessible to child classes.
     *
     * @param locationManager The LocationManager to retrieve locations from.
     */
    BaseLocationClient(@NonNull LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    @Override
    public final boolean isLocationAvailable() {
        return getProvider() != null;
    }

    protected String getProvider() {
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

    @Override
    public void setPriority(@NonNull Priority priority) {
        this.priority = priority;
    }

    @NonNull
    Priority getPriority() {
        return priority;
    }

    @NonNull
    LocationManager getLocationManager() {
        return locationManager;
    }
}
