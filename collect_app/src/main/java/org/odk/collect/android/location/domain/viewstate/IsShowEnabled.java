package org.odk.collect.android.location.domain.viewstate;

import android.support.annotation.NonNull;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.domain.state.CurrentLocation;
import org.odk.collect.android.location.domain.state.SelectedLocation;
import org.odk.collect.android.utilities.Rx;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerActivity
public class IsShowEnabled {

    @NonNull
    private final CurrentLocation currentLocation;

    @NonNull
    private final SelectedLocation selectedLocation;

    @Inject
    IsShowEnabled(@NonNull CurrentLocation currentLocation,
                  @NonNull SelectedLocation selectedLocation) {
        this.currentLocation = currentLocation;

        this.selectedLocation = selectedLocation;
    }

    public Observable<Boolean> observe() {
        return Observable.combineLatest(
                currentLocation.observePresence(),
                selectedLocation.observePresence(),
                Rx::or
        );
    }
}
