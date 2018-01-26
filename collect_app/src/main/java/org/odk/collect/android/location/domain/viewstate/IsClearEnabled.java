package org.odk.collect.android.location.domain.viewstate;

import android.support.annotation.NonNull;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.domain.state.SelectedLocation;
import org.odk.collect.android.location.injection.Qualifiers.HasInitialLocation;
import org.odk.collect.android.location.injection.Qualifiers.IsReadOnly;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerActivity
public class IsClearEnabled {

    @NonNull
    private final SelectedLocation selectedLocation;

    private final boolean isReadOnly;
    private final boolean hasInitialLocation;

    @Inject
    IsClearEnabled(@NonNull SelectedLocation selectedLocation,
                   @IsReadOnly boolean isReadOnly,
                   @HasInitialLocation boolean hasInitialLocation) {
        this.selectedLocation = selectedLocation;
        this.isReadOnly = isReadOnly;
        this.hasInitialLocation = hasInitialLocation;
    }

    public Observable<Boolean> observe() {
        return selectedLocation.observePresence()
                .map(hasSelectedLocation ->
                        !isReadOnly && (hasInitialLocation || hasSelectedLocation)
                );
    }

}
