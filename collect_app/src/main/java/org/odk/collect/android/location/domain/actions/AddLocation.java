package org.odk.collect.android.location.domain.actions;

import android.support.annotation.NonNull;

import com.google.common.base.Optional;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.domain.state.CurrentLocation;
import org.odk.collect.android.location.domain.state.SelectedLocation;
import org.odk.collect.android.location.domain.utility.LocationConverter;
import org.odk.collect.android.location.injection.Qualifiers.IsReadOnly;

import javax.inject.Inject;

import io.reactivex.Completable;

/**
 * Monitors the user's current location and selects it when requested,
 * assuming the 'isReadOnly' flag is not set.
 */
@PerActivity
public class AddLocation {

    @NonNull
    private final CurrentLocation currentLocation;

    @NonNull
    private final SelectedLocation selectedLocation;

    private final boolean isReadOnly;

    @Inject
    AddLocation(@NonNull CurrentLocation currentLocation,
                @NonNull SelectedLocation selectedLocation,
                @IsReadOnly boolean isReadOnly) {

        this.currentLocation = currentLocation;
        this.selectedLocation = selectedLocation;
        this.isReadOnly = isReadOnly;
    }

    public Completable add() {
        // If isReadOnly, return an empty action:
        if (isReadOnly) {
            return Completable.complete();
        }

        return currentLocation.observe()
                .firstOrError() // observe() starts with an absent Optional.
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(LocationConverter::locationToLatLng)
                .flatMapCompletable(selectedLocation::select);
    }
}
