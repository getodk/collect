package org.odk.collect.android.location.usecases;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.injection.config.scopes.PerViewModel;
import org.odk.collect.android.location.client.LocationClient;

import javax.inject.Inject;

import io.reactivex.Observable;
import timber.log.Timber;

@PerViewModel
public class WatchLocation {

    @NonNull
    private final LocationClient locationClient;

    @NonNull
    private final PublishRelay<Location> locationUpdates = PublishRelay.create();

    @NonNull
    private final BehaviorRelay<Boolean> isLocationAvailable = BehaviorRelay.create();

    @NonNull
    private final PublishRelay<Object> locationErrors = PublishRelay.create();

    private int locationCount = 0;

    @Inject
    WatchLocation(@NonNull LocationClient locationClient) {

        this.locationClient = locationClient;
        this.locationClient.setListener(new LocationClient.LocationClientListener() {
            @Override
            public void onClientStart() {
                locationClient.requestLocationUpdates(location -> {
                    if (location != null) {
                        Timber.i("onLocationChanged(%d) getLocation: %s", ++locationCount, location);
                        locationUpdates.accept(location);

                    } else {
                        Timber.i("onLocationChanged(%d) null getLocation.", ++locationCount);
                    }

                });
                if (!locationClient.isLocationAvailable()) {
                    isLocationAvailable.accept(false);
                }
            }

            @Override
            public void onClientStartFailure() {
                locationErrors.accept(this);
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

    Observable<Location> observeLocation() { return locationUpdates.hide(); }
    public Observable<Boolean> observeAvailability() {
        return isLocationAvailable.hide();
    }
    public Observable<Object> observeErrors() { return locationErrors.hide(); }
}