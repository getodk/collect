package org.odk.collect.android.location.domain.state;


import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.domain.utility.IsLocationValid;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

@PerActivity
public class CurrentLocation implements LocationClient.LocationClientListener {

    @NonNull
    private final LocationClient locationClient;

    @NonNull
    private final IsLocationValid isLocationValid;

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
    public CurrentLocation(@NonNull LocationClient locationClient,
                           @NonNull IsLocationValid isLocationValid) {
        this.locationClient = locationClient;
        this.locationClient.setListener(this);

        this.isLocationValid = isLocationValid;
    }

    public Observable<Optional<Location>> observe() {
        return locationRelay.hide();
    }

    public Single<Optional<Location>> get() {
        return locationRelay.hide()
                .firstOrError();
    }

    public Observable<Boolean> observePresence() {
        return locationRelay.hide()
                .doOnNext(Rx::log)
                .map(Optional::isPresent)
                .doOnNext(Rx::log)
                .distinctUntilChanged();
    }

    public Observable<Object> onError() {
        return locationErrors.hide();
    }


    private void receivedLocation(@Nullable Location location) {
        if (location == null) {
            Timber.i("onLocationChanged(%d) null getLocation.", ++locationCount);
            locationRelay.accept(Optional.absent());

            return;
        }

        Timber.i("onLocationChanged(%d) getLocation: %s", ++locationCount, location);
        if (!isLocationValid.isValid(location)) {
            Timber.w("onLocationChanged(%d) received invalid location.");
            return;
        }

        locationRelay.accept(Optional.of(location));
    }

    public void startLocation() {
        locationClient.start();
    }

    public void stopLocation() {
        locationClient.stop();
    }

    @Override
    public void onClientStart() {
        Timber.i("Location client started.");
        locationClient.requestLocationUpdates(this::receivedLocation);
        isLocationAvailable.accept(locationClient.isLocationAvailable());
    }

    @Override
    public void onClientStartFailure() {
        Timber.e("Location client failed.");
        locationErrors.accept(this);
    }

    @Override
    public void onClientStop() {
        Timber.i("Location client stopped.");
    }
}
