package org.odk.collect.android.location.domain.actions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.odk.collect.android.injection.scopes.PerActivity;
import org.odk.collect.android.location.domain.state.SelectedLocation;

import javax.inject.Inject;

import io.reactivex.Completable;

import static org.odk.collect.android.activities.FormEntryActivity.LOCATION_RESULT;

/**
 * Saves the currently selected location.
 */
@PerActivity
public class SaveAnswer {

    static final String SAVE_CONTEXT = "acceptLocation";
    static final String SAVE_ACTION = "OK";

    @NonNull
    private final SelectedLocation selectedLocation;

    @NonNull
    private final SetActivityResult setActivityResult;

    @Inject
    SaveAnswer(@NonNull SelectedLocation selectedLocation,
               @NonNull SetActivityResult setActivityResult) {
        this.selectedLocation = selectedLocation;
        this.setActivityResult = setActivityResult;
    }

    public Completable save() {
        return selectedLocation.get()
                .map(latLngOptional -> encodeLatLng(latLngOptional.orNull()))
                .flatMapCompletable(this::setAnswer);
    }

    @NonNull
    private String encodeLatLng(@Nullable LatLng latLng) {
        if (latLng == null) {
            return "";
        }

        return String.format("%s %s 0 0",
                latLng.latitude,
                latLng.longitude);
    }

    private Completable setAnswer(@NonNull String answer) {
        return setActivityResult.setAnswerForKey(
                LOCATION_RESULT,
                answer,
                "acceptLocation",
                "OK"
        );
    }
}
