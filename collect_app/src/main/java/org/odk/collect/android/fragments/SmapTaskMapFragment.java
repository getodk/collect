/*
 * Copyright (C) 2017 Smap Consulting Pty Ltd
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

package org.odk.collect.android.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
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

import org.odk.collect.android.R;
import org.odk.collect.android.activities.AboutActivity;
import org.odk.collect.android.activities.SmapMain;
import org.odk.collect.android.adapters.TaskListArrayAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.loaders.MapDataLoader;
import org.odk.collect.android.loaders.MapEntry;
import org.odk.collect.android.loaders.MapLocationObserver;
import org.odk.collect.android.loaders.PointEntry;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.tasks.DeleteInstancesTask;
import org.odk.collect.android.tasks.InstanceSyncTask;
import org.odk.collect.android.utilities.KeyValueJsonFns;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import timber.log.Timber;

/**
 * Responsible for displaying tasks on the main fieldTask screen
 */
public class SmapTaskMapFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<MapEntry>, OnMapReadyCallback {

    private static final int PASSWORD_DIALOG = 1;

    protected final ActivityLogger logger = Collect.getInstance().getActivityLogger();
    protected String[] sortingOptions;
    View rootView;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    protected LinearLayout searchBoxLayout;
    protected SimpleCursorAdapter listAdapter;
    protected LinkedHashSet<Long> selectedInstances = new LinkedHashSet<>();
    protected EditText inputSearch;

    private MapLocationObserver mo = null;
    private GoogleMap mMap;
    private Polyline mPath;
    private MapHelper mHelper;
    private ImageButton layers_button;
    private ImageButton location_button;

    private SharedPreferences adminPreferences;

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

    DeleteInstancesTask deleteInstancesTask = null;
    private AlertDialog alertDialog;
    private InstanceSyncTask instanceSyncTask;
    private static final int TASK_LOADER_ID = 1;
    private static final int MAP_LOADER_ID = 2;

    private TaskListArrayAdapter mAdapter;

    public static SmapTaskMapFragment newInstance() {
        return new SmapTaskMapFragment();
    }

    public SmapTaskMapFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.ft_map_layout, container, false);
        Timber.i("######## onCreateView");

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {

        Timber.i("######## onViewCreated");
        super.onViewCreated(rootView, savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle b) {
        adminPreferences = getActivity().getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);
        super.onActivityCreated(b);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle bundle) {
        super.onViewStateRestored(bundle);
    }


    @Override
    public void onPause() {

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!isVisibleToUser) {
            // close the drawer if open
            if (drawerLayout != null && drawerLayout.isDrawerOpen(Gravity.END)) {
                drawerLayout.closeDrawer(Gravity.END);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<MapEntry> loader) {

        clearTasks();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "onCreateOptionsMenu", "show");

        getActivity().getMenuInflater().inflate(R.menu.smap_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_about:
                Collect.getInstance()
                        .getActivityLogger()
                        .logAction(this, "onOptionsItemSelected",
                                "MENU_ABOUT");
                Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            case R.id.menu_general_preferences:
                Collect.getInstance()
                        .getActivityLogger()
                        .logAction(this, "onOptionsItemSelected",
                                "MENU_PREFERENCES");
                Intent ig = new Intent(getActivity(), PreferencesActivity.class);
                startActivity(ig);
                return true;
            case R.id.menu_admin_preferences:
                Collect.getInstance().getActivityLogger()
                        .logAction(this, "onOptionsItemSelected", "MENU_ADMIN");
                String pw = adminPreferences.getString(
                        AdminKeys.KEY_ADMIN_PW, "");
                if ("".equalsIgnoreCase(pw)) {
                    Intent i = new Intent(getActivity(),
                            AdminPreferencesActivity.class);
                    startActivity(i);
                } else {
                    ((SmapMain) getActivity()).processAdminMenu();
                    Collect.getInstance().getActivityLogger()
                            .logAction(this, "createAdminPasswordDialog", "show");
                }
                return true;
            case R.id.menu_gettasks:
                ((SmapMain) getActivity()).processGetTask();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Timber.i("######## onMapReady");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mHelper = new MapHelper(getActivity(),mMap);
        mHelper.setBasemap();

        userLocationIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_userlocation);
        accepted = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_open);
        repeat = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_repeat);
        rejected = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_reject);
        complete = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_done);
        submitted = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_submitted);
        triggered = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_triggered);
        triggered_repeat = BitmapDescriptorFactory.fromResource(R.drawable.ic_task_triggered_repeat);

        getLoaderManager().initLoader(MAP_LOADER_ID, null, this);       // Get the task locations
        mo = new MapLocationObserver(getContext(), this);

        location_button = getActivity().findViewById(R.id.show_location);
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


        layers_button = getActivity().findViewById(R.id.layers);
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

                LinearLayout info = new LinearLayout(getContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getContext());
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
                    Toast.makeText(getActivity(), "marker selected: " + selMarker.getTitle(), Toast.LENGTH_LONG).show();

                    Integer iPos = markerMap.get(selMarker);
                    if(iPos != null) {

                        int position = iPos;
                        List<TaskEntry> mapTasks = ((SmapMain) getActivity()).getMapTasks();
                        TaskEntry entry = mapTasks.get(position);

                        if(entry.locationTrigger != null) {
                            Toast.makeText(
                                    getActivity(),
                                    getString(R.string.smap_must_start_from_nfc),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            ((SmapMain) getActivity()).completeTask(entry);
                        }


                    }
                }

            }
        });
    }


    @Override
    public Loader<MapEntry> onCreateLoader(int id, Bundle args) {
        return new MapDataLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<MapEntry> loader, MapEntry data) {
        Timber.i( "######### Load Finished");
        ((SmapMain) getActivity()).setLocationTriggers(data.tasks, true);
        showTasks(data.tasks);
        showPoints(data.points);
        //zoomToData(false);
    }

    @Override
    public void onResume() {
        if(mHelper != null) {
            mHelper.setBasemap();
        }
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_launcher);
        super.onResume();
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
        if(mPath != null && mPoints != null) {
            mPoints.add(point);
            mPath.setPoints(mPoints);
        }
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
            Timber.i("Unknown task status: " + status);
            return accepted;
        }
    }

}
