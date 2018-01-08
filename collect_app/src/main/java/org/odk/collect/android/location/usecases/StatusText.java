package org.odk.collect.android.location.usecases;


import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.scopes.PerViewModel;

import java.text.DecimalFormat;

import io.reactivex.Observable;

@PerViewModel
public class StatusText {

    @NonNull
    private final Context context;

    @NonNull
    private final CurrentLocation currentLocation;

    @NonNull
    private final DecimalFormat decimalFormat;

    public StatusText(@NonNull Context context,
                      @NonNull CurrentLocation currentLocation,
                      @NonNull DecimalFormat decimalFormat) {
        this.context = context;
        this.currentLocation = currentLocation;
        this.decimalFormat = decimalFormat;
    }

    public Observable<String> observe() {
        return currentLocation.observe()
                .map(this::getStringForLocation)
                .startWith(getDefaultString());
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
}
