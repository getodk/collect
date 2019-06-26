package org.odk.collect.android.formentry.backgroundlocation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.location.client.GoogleLocationClient;

public class FormEntryViewModel extends ViewModel {
    @NonNull
    private final BackgroundLocationManager locationManager;

    public FormEntryViewModel() {
        locationManager = new BackgroundLocationManager(new GoogleLocationClient(Collect.getInstance().getApplicationContext()),
                new BackgroundLocationHelper());
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

    public void locationProvidersChanged() {
        locationManager.locationProvidersChanged();
    }

    public void backgroundLocationPreferenceToggled() {
        locationManager.backgroundLocationPreferenceToggled();
    }
}
