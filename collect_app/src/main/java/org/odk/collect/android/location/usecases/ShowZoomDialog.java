package org.odk.collect.android.location.usecases;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.scopes.PerActivity;

import javax.inject.Inject;

/**
 * @author James Knight
 */

@PerActivity
public class ShowZoomDialog {

    @NonNull
    private final Context context;

    private AlertDialog zoomDialog = null;

    private Location location = null;
    private LatLng latLng = null;

    private boolean setClear = false;

    @Inject
    public ShowZoomDialog(@NonNull Context context) {
        this.context = context;
    }

    public void show() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        @SuppressLint("InflateParams")
        View zoomDialogView = layoutInflater.inflate(R.layout.geopoint_zoom_dialog, null);

        if (zoomDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setTitle(R.string.zoom_to_where);
            builder.setView(zoomDialogView)
                    .setNegativeButton(R.string.cancel, (dialog, __) -> dialog.cancel())
                    .setOnCancelListener(DialogInterface::cancel);

            zoomDialog = builder.create();
        }

        Button zoomLocationButton = zoomDialogView.findViewById(R.id.zoom_location);
        Button zoomPointButton = zoomDialogView.findViewById(R.id.zoom_point);

        //If feature enable zoom to button else disable
        if (zoomLocationButton != null) {
            if (location != null) {
                zoomLocationButton.setEnabled(true);
                zoomLocationButton.setBackgroundColor(Color.parseColor("#50cccccc"));
                zoomLocationButton.setTextColor(Color.parseColor("#ff333333"));
            } else {
                zoomLocationButton.setEnabled(false);
                zoomLocationButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
                zoomLocationButton.setTextColor(Color.parseColor("#FF979797"));
            }

            if (latLng != null & !setClear) {
                zoomPointButton.setEnabled(true);
                zoomPointButton.setBackgroundColor(Color.parseColor("#50cccccc"));
                zoomPointButton.setTextColor(Color.parseColor("#ff333333"));
            } else {
                zoomPointButton.setEnabled(false);
                zoomPointButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
                zoomPointButton.setTextColor(Color.parseColor("#FF979797"));
            }
        }

        zoomDialog.show();
    }
}
