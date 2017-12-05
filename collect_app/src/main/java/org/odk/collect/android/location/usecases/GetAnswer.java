package org.odk.collect.android.location.usecases;


import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.odk.collect.android.injection.config.scopes.PerViewModel;

import javax.inject.Inject;

import io.reactivex.Maybe;

@PerViewModel
public class GetAnswer {

    private boolean setClear = false;
    private boolean readOnly = false;
    private boolean isDragged = false;
    private boolean locationFromIntent = false;

    private LatLng latLng = null;
    private Location location = null;

    @Inject
    public GetAnswer() {
    }

    public Maybe<String> get() {
        return Maybe.create(emitter -> {
            if (setClear || (readOnly && latLng == null)) {
                emitter.onSuccess("");

            } else if (isDragged || readOnly || locationFromIntent) {
                emitter.onSuccess(getResultString(latLng.latitude, latLng.longitude, 0, 0));

            } else if (location != null) {
                emitter.onSuccess(getResultString(location));

            } else {
                emitter.onComplete();
            }
        });
    }

    private String getResultString(Location location) {
        return getResultString(
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                location.getAccuracy()
        );
    }

    private String getResultString(double latitude, double longitude, double altitude, double accuracy) {
        return String.format("%s %s %s %s",
                latitude,
                longitude,
                altitude,
                accuracy);
    }
}
