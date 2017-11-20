package org.odk.collect.android.location.domain;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;

import org.odk.collect.android.R;
import org.odk.collect.android.location.GeoActivity;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.ToastUtils;

import javax.annotation.Nullable;

public class SetupMap {

    @NonNull
    private final GeoActivity activity;

    @Nullable
    private GoogleMap map;

    private MapHelper helper;

    private MarkerOptions markerOptions;

    public SetupMap(@NonNull GeoActivity activity) {
        this.activity = activity;
    }

    public void setMap(@Nullable GoogleMap googleMap) {
        map = googleMap;
        if (map == null) {
            ToastUtils.showShortToast(R.string.google_play_services_error_occured);
            activity.finish();
            return;
        }

        helper = new MapHelper(activity, map);

        map.setMyLocationEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);

        markerOptions = new MarkerOptions();

        // TODO: Setup Dialogs
//        zoomDialogView = getLayoutInflater().inflate(R.layout.geopoint_zoom_dialog, null);
//        zoomLocationButton = (Button) zoomDialogView.findViewById(R.id.zoom_location);
//        zoomLocationButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                zoomToLocation();
//                zoomDialog.dismiss();
//            }
//        });
//
//        zoomPointButton = (Button) zoomDialogView.findViewById(R.id.zoom_point);
//        zoomPointButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                zoomToPoint();
//                zoomDialog.dismiss();
//            }
//        });

//        Intent intent = getIntent();
//        if (intent != null && intent.getExtras() != null) {
//            if (intent.hasExtra(GeoPointWidget.DRAGGABLE_ONLY)) {
//                draggable = intent.getBooleanExtra(GeoPointWidget.DRAGGABLE_ONLY, false);
//                intentDraggable = draggable;
//                if (!intentDraggable) {
//                    // Not Draggable, set text for Map else leave as placement-map text
//                    locationInfo.setText(getString(R.string.geopoint_no_draggable_instruction));
//                }
//            }
//
//            if (intent.hasExtra(GeoPointWidget.READ_ONLY)) {
//                readOnly = intent.getBooleanExtra(GeoPointWidget.READ_ONLY, false);
//                if (readOnly) {
//                    captureLocation = true;
//                    clearPointButton.setEnabled(false);
//                }
//            }
//
//            if (intent.hasExtra(GeoPointWidget.LOCATION)) {
//                double[] location = intent.getDoubleArrayExtra(GeoPointWidget.LOCATION);
//                latLng = new LatLng(location[0], location[1]);
//                captureLocation = true;
//                reloadLocation.setEnabled(false);
//                draggable = false; // If data loaded, must clear first
//                locationFromIntent = true;
//
//            }
//        }
//        /*Zoom only if there's a previous location*/
//        if (latLng != null) {
//            locationInfo.setVisibility(View.GONE);
//            locationStatus.setVisibility(View.GONE);
//            showLocation.setEnabled(true);
//            markerOptions.position(latLng);
//            marker = map.addMarker(markerOptions);
//            captureLocation = true;
//            foundFirstLocation = true;
//            zoomToPoint();
//        }
//
//        helper.setBasemap();
//
//        isMapReady = true;
//        upMyLocationOverlayLayers();
    }
}
