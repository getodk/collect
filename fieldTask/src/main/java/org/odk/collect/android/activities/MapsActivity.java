/*
 * Copyright (C) 2011 Cloudtec Pty Ltd
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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.R;
import org.odk.collect.android.fragments.MapFragment;
import org.odk.collect.android.loaders.MapDataLoader;
import org.odk.collect.android.loaders.MapEntry;
import org.odk.collect.android.loaders.MapLocationObserver;
import org.odk.collect.android.loaders.PointEntry;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.utilities.KeyValueJsonFns;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.odk.collect.android.R.id.map;

/**
 * Responsible for displaying maps of tasks.
 * 
 * @author Neil Penman 
 */
public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<MapEntry> {

    private static final String TAG = "MapsActivity";
    private static final int MAP_LOADER_ID = 2;

    private GoogleMap mMap;
    private Polyline mPath;
    private MapHelper mHelper;
    private Button layers_button;
    private Button location_button;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private MapFragment mapFragment = null;
    private MapLocationObserver mo = null;

    private static MainTabsActivity tabsActivity;
    private static MapsActivity thisActivity;
    private static Context mContext;

    ArrayList<Marker> markers = null;
    HashMap<Marker, Integer> markerMap = null;
    ArrayList<LatLng> mPoints = new ArrayList<LatLng> ();

    Marker userLocationMarker = null;
    BitmapDescriptor userLocationIcon = null;
    BitmapDescriptor accepted = null;
    BitmapDescriptor repeat = null;
    BitmapDescriptor rejected = null;
    BitmapDescriptor complete = null;
    BitmapDescriptor submitted = null;
    BitmapDescriptor triggered = null;
    BitmapDescriptor triggered_repeat = null;

    private double tasksNorth;
    private double tasksSouth;
    private double tasksEast;
    private double tasksWest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        tabsActivity = (MainTabsActivity) getParent();
        thisActivity = this;
        mContext = getApplicationContext();

        try {
            setContentView(R.layout.ft_map_layout);
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), getString(R.string.google_play_services_error_occured),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userLocationIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_userlocation);
        accepted = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_open);
        repeat = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_repeat);
        rejected = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_reject);
        complete = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_done);
        submitted = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_submitted);
        triggered = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_triggered);
        triggered_repeat = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_triggered_repeat);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        mo = new MapLocationObserver(getApplicationContext(), thisActivity);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mHelper = new MapHelper(this,mMap);
        mHelper.setBasemap();

        location_button = (Button) findViewById(R.id.show_location);
        location_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Location location = mMap.getMyLocation();

                if (location != null) {
                    LatLng myLocation = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17));
                }

            }
        });


        layers_button = (Button)findViewById(R.id.layers);
        layers_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mHelper.showLayersDialog();

            }
        });

        /*
         * Add multiline info window
         * From: Hiren Patel, http://stackoverflow.com/questions/13904651/android-google-maps-v2-how-to-add-marker-with-multiline-snippet
         */
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(mContext);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(mContext);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(mContext);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        /*
         * Add long click listener
         */
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                // Get closest marker
                double minDistance = 1000;
                Marker selMarker = null;
                for(Marker marker : markers) {
                    double roughDistance = Math.sqrt(
                            Math.pow(marker.getPosition().latitude - latLng.latitude, 2) +
                            Math.pow(marker.getPosition().longitude - latLng.longitude, 2));
                    if(roughDistance < minDistance) {
                        minDistance = roughDistance;
                        selMarker = marker;
                    }
                }
                if(selMarker != null) {
                    Toast.makeText(thisActivity, "marker selected: " + selMarker.getTitle(), Toast.LENGTH_SHORT).show();

                    Integer iPos = markerMap.get(selMarker);
                    if(iPos != null) {

                        int position = iPos;
                        List<TaskEntry> mapTasks = tabsActivity.getMapTasks();
                        TaskEntry entry = mapTasks.get(position);

                        if(entry.locationTrigger != null) {
                            Toast.makeText(
                                    thisActivity,
                                    getString(R.string.smap_must_start_from_nfc),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            tabsActivity.completeTask(entry);
                        }


                    }
                }

            }
        });
        getSupportLoaderManager().initLoader(MAP_LOADER_ID, null, thisActivity);       // Get the task locations
    }

    @Override
    public Loader<MapEntry> onCreateLoader(int id, Bundle args) {
        return new MapDataLoader(this);
    }


    @Override
    public void onLoaderReset(Loader<MapEntry> loader) {

        clearTasks();
    }

    @Override
    public void onLoadFinished(Loader<MapEntry> loader, MapEntry data) {
        Log.i(TAG, "######### Load Finished");
        tabsActivity.setLocationTriggers(data.tasks, true);
        showTasks(data.tasks);
        showPoints(data.points);
        //zoomToData(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(mHelper != null) {
            mHelper.setBasemap();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void clearTasks() {

        if(markers != null && markers.size() > 0) {
            for (int i = 0; i < markers.size(); i++) {
                markers.get(i).remove();
            }
        }
    }

    private void showTasks(List<TaskEntry> data) {

        clearTasks();   // remove existing markers

        // Update markers
        markers = new ArrayList<Marker>();
        markerMap = new HashMap<Marker, Integer>();

        // Add the tasks to the marker array and to the map
        int index = 0;
        for(TaskEntry t : data) {
            if(t.type.equals("task")) {
                LatLng ll = getTaskCoords(t);
                if (ll != null) {
                    String taskTime = Utilities.getTaskTime(t.taskStatus, t.actFinish, t.taskStart);
                    String addressText = KeyValueJsonFns.getValues(t.taskAddress);
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(ll)
                            .title(t.name)
                            .snippet(taskTime + "\n" + addressText);

                    markerOptions.icon(getIcon(t.taskStatus, t.repeat, t.locationTrigger != null));

                    Marker m = mMap.addMarker(markerOptions);
                    markers.add(m);
                    markerMap.put(m, index);

                }
            }
            index++;
        }

    }

    private void showPoints(List<PointEntry> data) {

        mPoints = new ArrayList<LatLng> ();
        if(mPath != null) {
            mPath.remove();
        }
        mPath = mMap.addPolyline((new PolylineOptions()));

        for(PointEntry p : data) {
            mPoints.add(new LatLng(p.lat, p.lon));
        }
        mPath.setPoints(mPoints);
    }

    public void updatePath(LatLng point) {
        mPoints.add(point);
        mPath.setPoints(mPoints);
    }

    /*
     * Get the coordinates of the task and update the bounding box
     */
    private LatLng getTaskCoords(TaskEntry t) {

        double lat = 0.0;
        double lon = 0.0;
        LatLng locn = null;

        if((t.actLat == 0.0) && (t.actLon == 0.0)) {
            lat = t.schedLat;       // Scheduled coordinates of task
            lon = t.schedLon;
        } else  {
            lat = t.actLat;         // Actual coordinates of task
            lon = t.actLon;
        }

        if(lat != 0.0 && lon != 0.0) {
            // Update bounding box
            if(lat > tasksNorth) {
                tasksNorth = lat;
            }
            if(lat < tasksSouth) {
                tasksSouth = lat;
            }
            if(lon > tasksEast) {
                tasksEast = lon;
            }
            if(lat < tasksWest) {
                tasksWest = lon;
            }

            // Create Point
            locn = new LatLng(lat, lon);
        }


        return locn;
    }

    /*
     * Get the colour to represent the passed in task status
     */
    private BitmapDescriptor getIcon(String status, boolean isRepeat, boolean hasTrigger) {

        if(status.equals(Utilities.STATUS_T_REJECTED) || status.equals(Utilities.STATUS_T_CANCELLED)) {
            return rejected;
        } else if(status.equals(Utilities.STATUS_T_ACCEPTED)) {
            if(hasTrigger && !isRepeat) {
                return triggered;
            } else if (hasTrigger && isRepeat) {
               return triggered_repeat;
            } else if(isRepeat) {
                return repeat;
            } else {
                return accepted;
            }
        } else if(status.equals(Utilities.STATUS_T_COMPLETE)) {
            return complete;
        } else if(status.equals(Utilities.STATUS_T_SUBMITTED)) {
            return submitted;
        } else {
            Log.i(TAG, "Unknown task status: " + status);
            return accepted;
        }
    }
}