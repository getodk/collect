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

import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import org.odk.collect.android.spatial.MapHelper;
import android.view.View;

/**
 *  Abstracts the functionalities present in GeoPointGoogleMapActivity and GeoPointOsmMapActivity.
 *  @author mukund.code@gmail.com (Mukund Ananthu)
 */

public abstract class GeoPointMapActivity extends FragmentActivity {

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

}
