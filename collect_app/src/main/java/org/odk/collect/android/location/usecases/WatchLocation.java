package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;

import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.injection.config.scopes.PerViewModel;
import org.odk.collect.android.location.client.LocationClient;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerViewModel
public class WatchLocation {

    @NonNull
    private final LocationClient locationClient;

    @NonNull
    private final BehaviorRelay<Boolean> isLocationAvailable = BehaviorRelay.create();

    @Inject
    public WatchLocation(@NonNull LocationClient locationClient,
                         @NonNull CurrentLocation currentLocation) {

        this.locationClient = locationClient;
        this.locationClient.setListener(new LocationClient.LocationClientListener() {
            @Override
            public void onClientStart() {
                locationClient.requestLocationUpdates(currentLocation::update);
                if (!locationClient.isLocationAvailable()) {
                    isLocationAvailable.accept(false);
                }
            }

            @Override
            public void onClientStartFailure() {

            }

            @Override
            public void onClientStop() {

            }
        });
    }

    public void startWatching() {
        locationClient.start();
    }

    public void stopWatching() {
        locationClient.stop();
    }

    public Observable<Boolean> observeAvailability() {
        return isLocationAvailable.hide();
    }
}
