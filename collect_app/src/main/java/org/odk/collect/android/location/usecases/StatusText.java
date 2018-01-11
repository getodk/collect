package org.odk.collect.android.location.usecases;


import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.scopes.PerApplication;

import java.text.DecimalFormat;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import timber.log.Timber;

@PerApplication
public class StatusText extends Observable<String> {

    @NonNull
    private final Context context;

    @NonNull
    private final DecimalFormat decimalFormat;

    @NonNull
    private final Observable<String> statusText;

    @Inject
    StatusText(@NonNull Context context,
               @NonNull WatchPosition watchPosition,
               @NonNull DecimalFormat decimalFormat) {
        this.context = context;
        this.decimalFormat = decimalFormat;

        statusText = watchPosition.observeLocation()
                .doOnNext(__ -> Timber.i("StatusText: getting location."))
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

    @Override
    protected void subscribeActual(Observer<? super String> observer) {
        statusText.subscribe(observer);
    }
}
