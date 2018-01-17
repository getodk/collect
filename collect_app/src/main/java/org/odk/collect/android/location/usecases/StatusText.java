package org.odk.collect.android.location.usecases;


import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.view.View;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.scopes.PerApplication;

import java.text.DecimalFormat;

import javax.inject.Inject;

import io.reactivex.Observable;

@PerApplication
public class StatusText {

    @NonNull
    private final Context context;

    @NonNull
    private final DecimalFormat decimalFormat;

    @NonNull
    private final Observable<String> observeText;

    @NonNull
    private final Observable<Integer> observeVisibility;

    @Inject
    StatusText(@NonNull Context context,
               @NonNull WatchPosition watchPosition,
               @NonNull InitialLocation initialLocation,
               @NonNull WasCleared wasCleared,
               @NonNull DecimalFormat decimalFormat) {
        this.context = context;
        this.decimalFormat = decimalFormat;

        observeText = watchPosition.observeLocation()
                .map(currentLocation -> currentLocation.isPresent()
                        ? getStringForLocation(currentLocation.get())
                        : getDefaultString());

        observeVisibility = Observable.combineLatest(
                initialLocation.observePresence(),
                wasCleared.observe(),
                (hasInitial, hasBeenCleared) ->
                        hasInitial && !hasBeenCleared)
                .map(shouldHide -> shouldHide ? View.GONE : View.VISIBLE);
    }

    private String getStringForLocation(@NonNull Location location) {
        return context.getString(
                R.string.location_provider_accuracy,
                location.getProvider(),
                formatAccuracy(location.getAccuracy())
        );
    }

    private String formatAccuracy(float accuracy) {
        return decimalFormat.format(accuracy);
    }

    private String getDefaultString() {
        return context.getString(R.string.please_wait_long);
    }

    @NonNull
    public Observable<String> observeText() {
        return observeText;
    }

    @NonNull
    public Observable<Integer> observeVisibility() {
        return observeVisibility;
    }
}
