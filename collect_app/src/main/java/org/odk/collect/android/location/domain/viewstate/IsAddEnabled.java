package org.odk.collect.android.location.domain.viewstate;

import android.support.annotation.NonNull;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.domain.state.CurrentLocation;
import org.odk.collect.android.location.injection.Qualifiers.IsReadOnly;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author James Knight
 */

@PerActivity
public class IsAddEnabled {

    @NonNull
    private final CurrentLocation currentLocation;

    private final boolean isReadOnly;

    @Inject
    IsAddEnabled(@NonNull CurrentLocation currentLocation,
                 @IsReadOnly boolean isReadOnly) {
        this.currentLocation = currentLocation;

        this.isReadOnly = isReadOnly;
    }

    public Observable<Boolean> observe() {
        return currentLocation.observePresence()
                .map(hasCurrentPosition -> !isReadOnly && hasCurrentPosition);
    }
}
