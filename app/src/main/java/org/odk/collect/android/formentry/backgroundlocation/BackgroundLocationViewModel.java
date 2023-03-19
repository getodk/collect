package org.odk.collect.android.formentry.backgroundlocation;

import static org.odk.collect.settings.keys.ProjectKeys.KEY_BACKGROUND_LOCATION;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.shared.settings.Settings;

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
}
