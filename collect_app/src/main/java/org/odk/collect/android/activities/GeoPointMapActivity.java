/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.spatial.MapHelper;
import android.view.View;
import java.text.DecimalFormat;
import java.util.List;

/**
 *  Abstracts the functionalities present in GeoPointGoogleMapActivity and GeoPointOsmMapActivity.
 *  @author mukund.code@gmail.com (Mukund Ananthu)
 */

public abstract class GeoPointMapActivity extends FragmentActivity implements LocationListener {

    protected static final String LOCATION_COUNT = "locationCount";
    protected TextView locationStatus;
    protected LocationManager locationManager;
    protected Location location;
    protected Button reloadLocationButton;
    protected boolean isDragged = false;
    protected Button showLocationButton;
    protected boolean gpsOn = false;
    protected boolean networkOn = false;
    protected int locationCount = 0;
    protected MapHelper helper;
    protected AlertDialog zoomDialog;
    protected View zoomDialogView;
    protected Button zoomPointButton;
    protected Button zoomLocationButton;
    protected boolean setClear = false;
    protected boolean captureLocation = false;
    protected Boolean foundFirstLocation = false;
    protected int locationCountNum = 0;
    protected int locationCountFoundLimit = 1;
    protected Boolean readOnly = false;
    protected Boolean draggable = false;
    protected Boolean intentDraggable = false;
    protected Boolean locationFromIntent = false;

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }

    protected String truncateFloat(float f) {
        return new DecimalFormat("#.##").format(f);
    }

    protected void upMyLocationOverlayLayers() {
        // make sure we have a good location provider before continuing
        locationCountNum = 0;
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                gpsOn = true;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                locationCountFoundLimit = 0;
            } else if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                // Only if GPS Provider is not available use network location. bug (well know
                // android bug) http://stackoverflow
                // .com/questions/6719207/locationmanager-returns-old-cached-wifi-location-with
                // -current-timestamp
                networkOn = true;
                locationCountFoundLimit =
                        1; // increase count due to network location bug (well know android bug)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        this);
            }
        }
        //showLocationButton.setClickable(marker != null);
        if (!gpsOn && !networkOn) {
            showGPSDisabledAlertToUser();
        } else {
            overlayMyLocationLayers();
        }
    }

    protected abstract void overlayMyLocationLayers();

    protected void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.gps_enable_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.enable_gps),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivityForResult(
                                        new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                            }
                        });
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public abstract void onLocationChanged(Location location);

    protected abstract void zoomToLocation();

    protected abstract void zoomToPoint();

    public abstract void showZoomDialog();

}
