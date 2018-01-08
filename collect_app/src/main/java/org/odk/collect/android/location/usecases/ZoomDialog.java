package org.odk.collect.android.location.usecases;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.scopes.PerActivity;
import org.odk.collect.android.location.model.LocationState;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author James Knight
 */

@PerActivity
public class ZoomDialog {

    private static final int BUTTON_ENABLED_COLOR = Color.parseColor("#50cccccc");
    private static final int TEXT_ENABLED_COLOR = Color.parseColor("#ff333333");

    private static final int BUTTON_DISABLED_COLOR = Color.parseColor("#50e2e2e2");
    private static final int TEXT_DISABLED_COLOR = Color.parseColor("#FF979797");

    @NonNull
    private final Context context;

    @NonNull
    private final PublishRelay<LatLng> zoomToLocation = PublishRelay.create();

    @Inject
    public ZoomDialog(@NonNull Context context) {
        this.context = context;
    }

    public void show(LocationState locationState) {

        View zoomDialogView = getView(locationState);

        new AlertDialog.Builder(context)
                .setView(zoomDialogView)
                .setTitle(R.string.zoom_to_where)
                .setNegativeButton(R.string.cancel, (d, __) -> d.cancel())
                .setOnCancelListener(DialogInterface::cancel)
                .show();
    }

    private View getView(LocationState locationState) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);

        @SuppressLint("InflateParams")
        View zoomDialogView = layoutInflater.inflate(R.layout.geopoint_zoom_dialog, null);

        Button zoomLocationButton = zoomDialogView.findViewById(R.id.zoom_location);
        setupZoomToLocationButton(zoomLocationButton, locationState.getCurrentLocation());

        Button zoomPointButton = zoomDialogView.findViewById(R.id.zoom_point);
        setupZoomToPointButton(zoomPointButton, locationState.getMarkedLocation());

        return zoomDialogView;
    }

    private void setupZoomToLocationButton(@NonNull Button zoomLocationButton,
                                           @Nullable LatLng currentLocation) {

        zoomLocationButton.setEnabled(currentLocation != null);
        zoomLocationButton.setBackgroundColor(currentLocation != null ? BUTTON_ENABLED_COLOR : BUTTON_DISABLED_COLOR);
        zoomLocationButton.setTextColor(currentLocation != null ? TEXT_ENABLED_COLOR : TEXT_DISABLED_COLOR);

        if (currentLocation != null) {
            zoomLocationButton.setOnClickListener(__ -> zoomToLocation(currentLocation));
        }
    }

    private void setupZoomToPointButton(@NonNull Button zoomPointButton,
                                        @Nullable LatLng markedLocation) {

        zoomPointButton.setEnabled(markedLocation != null);
        zoomPointButton.setBackgroundColor(markedLocation != null ? BUTTON_ENABLED_COLOR : BUTTON_DISABLED_COLOR);
        zoomPointButton.setTextColor(markedLocation != null ? TEXT_ENABLED_COLOR : TEXT_DISABLED_COLOR);

        if (markedLocation != null) {
            zoomPointButton.setOnClickListener(__ -> zoomToLocation(markedLocation));
        }
    }

    private void zoomToLocation(@NonNull LatLng latLng) {
        zoomToLocation.accept(latLng);
    }

    public Observable<LatLng> zoomToLocation() {
        return zoomToLocation.hide();
    }
}
