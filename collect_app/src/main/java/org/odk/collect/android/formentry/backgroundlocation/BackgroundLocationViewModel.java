package org.odk.collect.android.formentry.backgroundlocation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.location.client.GoogleFusedLocationClient;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_BACKGROUND_LOCATION;

/**
 * Ensures that background location tracking continues throughout the activity lifecycle. Builds
 * location-related dependency, receives activity events from #{@link FormEntryActivity} and
 * forwards those events to the location manager.
 *
 * The current goal is to keep this component very thin but this may evolve as it is involved in
 * managing more model objects.
 */
public class BackgroundLocationViewModel extends ViewModel {
    @NonNull
    private final BackgroundLocationManager locationManager;

    public BackgroundLocationViewModel(BackgroundLocationManager locationManager) {
        this.locationManager = locationManager;
    }

    public void formFinishedLoading() {
        locationManager.formFinishedLoading();
    }

    public BackgroundLocationManager.BackgroundLocationMessage activityDisplayed() {
        return locationManager.activityDisplayed();
    }

    public void activityHidden() {
        locationManager.activityHidden();
    }

    public boolean isBackgroundLocationPermissionsCheckNeeded() {
        return locationManager.isPendingPermissionCheck();
    }

    public BackgroundLocationManager.BackgroundLocationMessage locationPermissionsGranted() {
        return locationManager.locationPermissionGranted();
    }

    public void locationPermissionsDenied() {
        locationManager.locationPermissionDenied();
    }

    public void locationPermissionChanged() {
        locationManager.locationPermissionChanged();
    }

    public void locationProvidersChanged() {
        locationManager.locationProvidersChanged();
    }

    public void backgroundLocationPreferenceToggled() {
        GeneralSharedPreferences.getInstance().save(KEY_BACKGROUND_LOCATION, !GeneralSharedPreferences.getInstance().getBoolean(KEY_BACKGROUND_LOCATION, true));
        locationManager.backgroundLocationPreferenceToggled();
    }

    public static class Factory implements ViewModelProvider.Factory {
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.equals(BackgroundLocationViewModel.class)) {
                GoogleFusedLocationClient googleLocationClient = new GoogleFusedLocationClient(Collect.getInstance());

                BackgroundLocationManager locationManager =
                        new BackgroundLocationManager(googleLocationClient, new BackgroundLocationHelper());
                return (T) new BackgroundLocationViewModel(locationManager);
            }
            return null;
        }
    }
}
