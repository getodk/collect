package org.odk.collect.android.formentry.backgroundlocation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.location.GoogleFusedLocationClient;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.shared.Settings;

import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_BACKGROUND_LOCATION;

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

    public void backgroundLocationPreferenceToggled(Settings generalSettings) {
        generalSettings.save(KEY_BACKGROUND_LOCATION, !generalSettings.getBoolean(KEY_BACKGROUND_LOCATION));
        locationManager.backgroundLocationPreferenceToggled();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final PermissionsProvider permissionsProvider;
        private final Settings generalSettings;

        public Factory(PermissionsProvider permissionsProvider, Settings generalSettings) {
            this.permissionsProvider = permissionsProvider;
            this.generalSettings = generalSettings;
        }

        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.equals(BackgroundLocationViewModel.class)) {
                GoogleFusedLocationClient googleLocationClient = new GoogleFusedLocationClient(Collect.getInstance());

                BackgroundLocationManager locationManager =
                        new BackgroundLocationManager(googleLocationClient, new BackgroundLocationHelper(permissionsProvider, generalSettings));
                return (T) new BackgroundLocationViewModel(locationManager);
            }
            return null;
        }
    }
}
