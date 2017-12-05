package org.odk.collect.android.location.usecases;

import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import org.odk.collect.android.widgets.GeoPointWidget;

import javax.annotation.Nullable;

public class ReadParameters {

    private boolean isDraggable;
    private boolean isReadOnly;
    private boolean captureLocation;
    private boolean isIntentDraggable;
    private boolean isLocationFromIntent;
    private LatLng latLng;

    public void get(@Nullable Bundle extras) {
        if (extras == null) return;

        if (extras.containsKey(GeoPointWidget.DRAGGABLE_ONLY)) {
            isDraggable = extras.getBoolean(GeoPointWidget.DRAGGABLE_ONLY, false);
            isIntentDraggable = isDraggable;

            // locationInfo.setText(getString(R.string.geopoint_no_draggable_instruction));
        }

        if (extras.containsKey(GeoPointWidget.READ_ONLY)) {
            isReadOnly = extras.getBoolean(GeoPointWidget.READ_ONLY, false);
            if (isReadOnly) {
                captureLocation = true;
                // clearPointButton.setEnabled(false);
            }
        }

        if (extras.containsKey(GeoPointWidget.LOCATION)) {
            double[] location = extras.getDoubleArray(GeoPointWidget.LOCATION);
            latLng = new LatLng(location[0], location[1]);
            captureLocation = true;
//            reloadLocation.setEnabled(false);
            isDraggable = false; // If data loaded, must clear first
            isLocationFromIntent = true;
        }
    }
}
