package org.odk.collect.android.location.usecases;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.location.client.LocationClient;

import java.util.Date;

import javax.inject.Inject;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

@PerApplication
public class WatchPosition {

    @NonNull
    private final LocationClient locationClient;

    @NonNull
    private final BehaviorRelay<Optional<Location>> locationRelay =
            BehaviorRelay.createDefault(Optional.absent());

    @NonNull
    private final BehaviorRelay<Boolean> isLocationAvailable =
            BehaviorRelay.create();

    @NonNull
    private final PublishRelay<Object> locationErrors =
            PublishRelay.create();

    private int locationCount = 0;

    @Inject
    WatchPosition(@NonNull LocationClient locationClient) {

        this.locationClient = locationClient;
        this.locationClient.setListener(new LocationClient.LocationClientListener() {
            @Override
            public void onClientStart() {
                locationClient.requestLocationUpdates(location -> {
                    if (location != null) {
                        Timber.i("onLocationChanged(%d) getLocation: %s", ++locationCount, location);

                        long millis = new Date().getTime() - location.getTime();
                        if (millis <= 5 * 1_000) {
                            WatchPosition.this.locationRelay.accept(Optional.of(location));
                        }

                    } else {
                        Timber.i("onLocationChanged(%d) null getLocation.", ++locationCount);
                        WatchPosition.this.locationRelay.accept(Optional.absent());
                    }

                });

                boolean isAvailable = locationClient.isLocationAvailable();
                isLocationAvailable.accept(isAvailable);
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

    public Observable<Optional<Location>> observeLocation() {
        return locationRelay.hide();
    }

    public Single<Optional<Location>> currentLocation() {
        return observeLocation().first(Optional.absent());
    }

    public Observable<Boolean> observeAvailability() {
        return isLocationAvailable.hide();
    }

    public Observable<Object> observeErrors() { return locationErrors.hide(); }
}