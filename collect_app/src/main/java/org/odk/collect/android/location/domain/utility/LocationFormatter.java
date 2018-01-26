package org.odk.collect.android.location.domain.utility;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.scopes.PerActivity;

import java.text.DecimalFormat;

import javax.inject.Inject;

@PerActivity
public class LocationFormatter {

    @NonNull
    private final Context context;

    @NonNull
    private final DecimalFormat decimalFormat;

    @Inject
    LocationFormatter(@NonNull Context context,
                      @NonNull DecimalFormat decimalFormat) {
        this.context = context;
        this.decimalFormat = decimalFormat;
    }

    public String getStringForLocation(@NonNull Location location) {
        return context.getString(
                R.string.location_provider_accuracy,
                location.getProvider(),
                formatAccuracy(location.getAccuracy())
        );
    }

    private String formatAccuracy(float accuracy) {
        return decimalFormat.format(accuracy);
    }

}
