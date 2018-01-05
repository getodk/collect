package org.odk.collect.android.location.usecases;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.odk.collect.android.injection.config.scopes.PerViewModel;
import org.odk.collect.android.widgets.GeoPointWidget;

import javax.inject.Inject;

/**
 * @author James Knight
 */

@PerViewModel
public class InitialState {

    private final boolean isDraggable;
    private final boolean isReadOnly;

    @Nullable
    private final LatLng location;

    @Inject
    InitialState(@NonNull Activity activity) {
        Intent intent = activity.getIntent();
        if (intent == null || intent.getExtras() == null) {
            isDraggable = false;
            isReadOnly = false;
            location = null;
            return;
        }

        isDraggable = intent.hasExtra(GeoPointWidget.DRAGGABLE_ONLY)
                && intent.getBooleanExtra(GeoPointWidget.DRAGGABLE_ONLY, false);

        isReadOnly = intent.hasExtra(GeoPointWidget.READ_ONLY)
                && intent.getBooleanExtra(GeoPointWidget.READ_ONLY, false);

        if (intent.hasExtra(GeoPointWidget.LOCATION)) {
            double[] locationArray = intent.getDoubleArrayExtra(GeoPointWidget.LOCATION);
            location = new LatLng(locationArray[0], locationArray[1]);
        } else {
            location = null;
        }
    }

    public boolean isDraggable() {
        return isDraggable;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    @Nullable
    public LatLng getLocation() {
        return location;
    }
}
