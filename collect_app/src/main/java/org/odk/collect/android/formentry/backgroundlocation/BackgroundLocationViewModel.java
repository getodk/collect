package org.odk.collect.android.formentry.backgroundlocation;

import static org.odk.collect.settings.keys.ProjectKeys.KEY_BACKGROUND_LOCATION;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.formentry.FormSessionRepository;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.location.LocationClient;
import org.odk.collect.permissions.PermissionsProvider;
import org.odk.collect.shared.settings.Settings;

import javax.inject.Inject;
import javax.inject.Named;

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
        private final FormSessionRepository formSessionRepository;
        private final String sessionId;

        /**
         * It's not clear why this needs to use the {@link org.odk.collect.location.GoogleFusedLocationClient}
         */
        @Inject
        @Named("fused")
        LocationClient fusedLocatonClient;

        public Factory(PermissionsProvider permissionsProvider, Settings generalSettings, FormSessionRepository formSessionRepository, String sessionId) {
            this.permissionsProvider = permissionsProvider;
            this.generalSettings = generalSettings;
            this.formSessionRepository = formSessionRepository;
            this.sessionId = sessionId;

            DaggerUtils.getComponent(Collect.getInstance()).inject(this);
        }

        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.equals(BackgroundLocationViewModel.class)) {
                BackgroundLocationManager locationManager =
                        new BackgroundLocationManager(fusedLocatonClient, new BackgroundLocationHelper(permissionsProvider, generalSettings, () -> formSessionRepository.get(sessionId).getValue()));
                return (T) new BackgroundLocationViewModel(locationManager);
            }
            return null;
        }
    }
}
