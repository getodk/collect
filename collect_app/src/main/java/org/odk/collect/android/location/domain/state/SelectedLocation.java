package org.odk.collect.android.location.domain.state;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.injection.Qualifiers.IsReadOnly;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

@PerActivity
public class SelectedLocation {

    @NonNull
    private final MarkInitialLocation markInitialLocation;
    private final boolean isReadOnly;

    @NonNull
    private final BehaviorRelay<Optional<LatLng>> selectedLocationRelay =
            BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<Boolean> hasBeenClearedRelay =
            BehaviorRelay.createDefault(false);


    @Inject
    public SelectedLocation(@NonNull MarkInitialLocation markInitialLocation,
                            @IsReadOnly boolean isReadOnly) {
        this.markInitialLocation = markInitialLocation;
        this.isReadOnly = isReadOnly;
    }

    public Observable<Optional<LatLng>> observe() {
        return Observable.merge(
                selectedLocationRelay.hide(),
                markInitialLocation.observe()
                        .map(Optional::of)
        );
    }

    public Single<Optional<LatLng>> get() {
        return observe().firstOrError();
    }

    public Observable<Boolean> observePresence() {
        return observe()
                .doOnNext(it -> Timber.i("Log: %s", it))
                .map(Optional::isPresent)
                .doOnNext(it -> Timber.i("Log: %s", it))
                .distinctUntilChanged();
    }

    public Completable select(@Nullable LatLng latLng) {
        return Completable.defer(() -> {
            if (!isReadOnly) {
                selectedLocationRelay.accept(Optional.fromNullable(latLng));

                if (latLng == null) {
                    hasBeenClearedRelay.accept(true);
                }
            }

            return Completable.complete();
        });
    }


    public Observable<Boolean> hasBeenCleared() {
        return hasBeenClearedRelay.hide()
                .distinctUntilChanged();
    }
}
