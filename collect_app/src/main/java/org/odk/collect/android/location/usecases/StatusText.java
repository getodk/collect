package org.odk.collect.android.location.usecases;


import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

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
    private final CurrentPosition currentPosition;

    @NonNull
    private final DecimalFormat decimalFormat;

    @Inject
    public StatusText(@NonNull Context context,
                      @NonNull CurrentPosition currentPosition,
                      @NonNull DecimalFormat decimalFormat) {
        this.context = context;
        this.currentPosition = currentPosition;
        this.decimalFormat = decimalFormat;
    }

    public Observable<String> observe() {
        return currentPosition.observe()
                .map(currentLocation -> currentLocation.isPresent()
                        ? getStringForLocation(currentLocation.get())
                        : getDefaultString());
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
