package org.odk.collect.android.location.domain.viewstate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.domain.state.CurrentLocation;
import org.odk.collect.android.location.domain.utility.LocationFormatter;
import org.odk.collect.android.location.domain.state.SelectedLocation;
import org.odk.collect.android.location.injection.Qualifiers.HasInitialLocation;
import org.odk.collect.android.location.injection.Qualifiers.IsReadOnly;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerActivity
public class StatusText {
    @NonNull
    private final Context context;

    @NonNull
    private final CurrentLocation currentLocation;

    @NonNull
    private final SelectedLocation selectedLocation;

    @NonNull
    private final LocationFormatter locationFormatter;

    private final boolean isReadOnly;
    private final boolean hasInitialLocation;

    @Inject
    public StatusText(@NonNull Context context,
                      @NonNull CurrentLocation currentLocation, @NonNull SelectedLocation selectedLocation,
                      @NonNull LocationFormatter locationFormatter,
                      @IsReadOnly boolean isReadOnly,
                      @HasInitialLocation boolean hasInitialLocation) {
        this.context = context;
        this.currentLocation = currentLocation;
        this.selectedLocation = selectedLocation;
        this.locationFormatter = locationFormatter;

        this.isReadOnly = isReadOnly;
        this.hasInitialLocation = hasInitialLocation;
    }

    public Observable<String> observeText() {
        return currentLocation.observe()
                .map(locationOptional -> locationOptional.isPresent()
                        ? locationFormatter.getStringForLocation(locationOptional.get())
                        : context.getString(R.string.please_wait_long)
                );
    }

    public Observable<Integer> observeVisibility() {
        return selectedLocation.hasBeenCleared()
                .map(this::shouldHide);
    }

    private int shouldHide(boolean wasCleared) {
        return isReadOnly || (hasInitialLocation && !wasCleared)
                ? View.GONE
                : View.VISIBLE;
    }
}
