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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
import org.odk.collect.android.activities.viewmodels.SurveyDataViewModel;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.loaders.MapLocationObserver;
import org.odk.collect.android.loaders.PointEntry;
import org.odk.collect.android.loaders.SurveyData;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.KeyValueJsonFns;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import timber.log.Timber;

/**
 * Responsible for displaying tasks on the main fieldTask screen
 */
public class SmapTaskMapFragment extends Fragment
        implements  OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    private static final int REQUEST_LOCATION = 100;

    View rootView;

    protected LinearLayout searchBoxLayout;
    protected SimpleCursorAdapter listAdapter;
    protected LinkedHashSet<Long> selectedInstances = new LinkedHashSet<>();

    private MapLocationObserver mo = null;
    private GoogleMap mMap;
    private Polyline mPath;
    private ImageButton layers_button;
    private ImageButton location_button;

    private SharedPreferences adminPreferences;

    ArrayList<Marker> markers = null;
    HashMap<Marker, Integer> markerMap = null;
    ArrayList<LatLng> mPoints = new ArrayList<LatLng> ();

    BitmapDescriptor complete = null;
    BitmapDescriptor accepted = null;
    BitmapDescriptor late = null;
    BitmapDescriptor repeat = null;
    BitmapDescriptor rejected = null;
    BitmapDescriptor newtask = null;
    BitmapDescriptor submitted = null;
    BitmapDescriptor triggered = null;
    BitmapDescriptor triggered_repeat = null;

    private double tasksNorth;
    private double tasksSouth;
    private double tasksEast;
    private double tasksWest;

    SurveyDataViewModel model;

    @Inject
    PermissionsProvider permissionsProvider;

    public static SmapTaskMapFragment newInstance() {
        return new SmapTaskMapFragment();
    }

    public SmapTaskMapFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.ft_map_layout, container, false);
        Timber.i("######## onCreateView");

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {

        Timber.i("######## onViewCreated");
        super.onViewCreated(rootView, savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(SurveyDataViewModel.class);
        model.getSurveyData().observe(getViewLifecycleOwner(), surveyData -> {
            Timber.i("-------------------------------------- Task Map Fragment got Data ");
            setData(surveyData);
        });

    }

    @Override
    public void onDestroyView() {
        rootView = null;
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle b) {
        adminPreferences = getActivity().getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

        super.onActivityCreated(b);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle bundle) {
        super.onViewStateRestored(bundle);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        getActivity().getMenuInflater().inflate(R.menu.smap_menu_map, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_about:
                Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            case R.id.menu_general_preferences:
                Intent ig = new Intent(getActivity(), PreferencesActivity.class);
                startActivity(ig);
                return true;
            case R.id.menu_admin_preferences:
                String pw = adminPreferences.getString(
                        AdminKeys.KEY_ADMIN_PW, "");
                if ("".equalsIgnoreCase(pw)) {
                    Intent i = new Intent(getActivity(),
                            AdminPreferencesActivity.class);
                    startActivity(i);
                } else {
                    ((SmapMain) getActivity()).processAdminMenu();
                }
                return true;
            case R.id.menu_gettasks:
                ((SmapMain) getActivity()).processGetTask(true);
                return true;
            case R.id.menu_history:
                ((SmapMain) getActivity()).processHistory();
                return true;
            case R.id.menu_exit:
                ((SmapMain) getActivity()).exit();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Timber.i("######## onMapReady");
        mMap = googleMap;

        permissionsProvider.requestLocationPermissions((Activity) getContext(), new PermissionListener() {
            @Override
            public void granted() {
                ((SmapMain) getActivity()).startLocationService();
                mapReadyPermissionGranted();
            }

            @Override
            public void denied() {
            }
        });


    }

    @SuppressLint("MissingPermission")
    private void mapReadyPermissionGranted() {

        if(mo == null) {

            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);

            mMap.setMyLocationEnabled(true);

            complete = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.form_state_finalized_circle));
            accepted = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.form_state_saved_circle));
            late = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.form_state_late));
            repeat = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.form_state_repeat));
            rejected = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.form_state_rejected));
            newtask = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.form_state_new));
            submitted = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.form_state_submitted_circle));
            triggered = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.form_state_triggered));
            triggered_repeat = getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.form_state_triggered));


            mo = new MapLocationObserver(getContext(), this);

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
                    if (markers != null) {
                        for (Marker marker : markers) {
                            double roughDistance = Math.sqrt(
                                    Math.pow(marker.getPosition().latitude - latLng.latitude, 2) +
                                            Math.pow(marker.getPosition().longitude - latLng.longitude, 2));
                            if (roughDistance < minDistance) {
                                minDistance = roughDistance;
                                selMarker = marker;
                            }
                        }
                    }
                    if (selMarker != null) {
                        Toast.makeText(getActivity(), "marker selected: " + selMarker.getTitle(), Toast.LENGTH_LONG).show();

                        Integer iPos = markerMap.get(selMarker);
                        if (iPos != null) {

                            int position = iPos;
                            List<TaskEntry> tasks = ((SmapMain) getActivity()).getTasks();
                            TaskEntry entry = tasks.get(position);

                            if (entry.locationTrigger != null) {
                                Toast.makeText(
                                        getActivity(),
                                        getString(R.string.smap_must_start_from_nfc),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                ((SmapMain) getActivity()).completeTask(entry, false);
                            }


                        }
                    }

                }
            });

            // Refresh the data
            Intent intent = new Intent("org.smap.smapTask.refresh");
            LocalBroadcastManager.getInstance(Collect.getInstance()).sendBroadcast(intent);
            Timber.i("######## send org.smap.smapTask.refresh from smapTaskMapFragment");
        }
    }

    public void setData(SurveyData data) {
        if(data != null) {
            showTasks(data.tasks);
            showPoints(data.points);
        } else {
            clearTasks();
        }
    }

    @Override
    public void onResume() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_nav_foreground);
        model.loadData();   // Update the user trail display with latest points
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

        if(mMap != null) {

            clearTasks();   // remove existing markers

            // Update markers
            markers = new ArrayList<Marker>();
            markerMap = new HashMap<Marker, Integer>();

            // Add the tasks to the marker array and to the map
            int index = 0;
            for (TaskEntry t : data) {
                if (t.type.equals("task")) {
                    LatLng ll = getTaskCoords(t);
                    if (ll != null) {
                        String taskTime = Utilities.getTaskTime(t.taskStatus, t.actFinish, t.taskStart);
                        String addressText = KeyValueJsonFns.getValues(t.taskAddress);
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(ll)
                                .title(t.name)
                                .snippet(taskTime + "\n" + addressText);

                        markerOptions.icon(getIcon(t.taskStatus, t.repeat, t.locationTrigger != null, t.taskFinish));
                        Marker m = mMap.addMarker(markerOptions);
                        markers.add(m);
                        markerMap.put(m, index);

                    }
                }
                index++;
            }
        }

    }

    private void showPoints(List<PointEntry> data) {

        if(mMap != null) {
            mPoints = new ArrayList<LatLng>();
            if (mPath != null) {
                mPath.remove();
            }
            mPath = mMap.addPolyline((new PolylineOptions()));

            //Add in reverse order
            for(int i = data.size() - 1; i >= 0; i--) {
                PointEntry p = data.get(i);
                mPoints.add(new LatLng(p.lat, p.lon));
            }
            mPath.setPoints(mPoints);
        }
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
     * Get the icon to represent the passed in task status
     */
    private BitmapDescriptor getIcon(String status, boolean isRepeat, boolean hasTrigger, long taskFinish) {

        if(status.equals(Utilities.STATUS_T_REJECTED) || status.equals(Utilities.STATUS_T_CANCELLED)) {
            return rejected;
        } else if(status.equals(Utilities.STATUS_T_ACCEPTED)) {
            if(hasTrigger && !isRepeat) {
                return triggered;
            } else if (hasTrigger && isRepeat) {
                return triggered_repeat;
            } else if(isRepeat) {
                return repeat;
            } else if(taskFinish != 0 && taskFinish < (new Date()).getTime()) {
                return late;
            } else {
                return accepted;
            }
        } else if(status.equals(Utilities.STATUS_T_COMPLETE)) {
            return complete;
        } else if(status.equals(Utilities.STATUS_T_SUBMITTED)) {
            return submitted;
        } else if(status.equals(Utilities.STATUS_T_NEW)) {
            return newtask;
        } else {
            Timber.i("Unknown task status: " + status);
            return accepted;
        }
    }

    /*
     * Convert an xml drawable to a bitmap
     * From: https://stackoverflow.com/questions/18053156/set-image-from-drawable-as-marker-in-google-map-version-2
     */
    @TargetApi(19)
    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {

        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                bitmap = bitmapDrawable.getBitmap();
            }
        }

        if(bitmap == null) {
            if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            float maxHeight = displaymetrics.heightPixels / 15;

            int intHeight = drawable.getIntrinsicHeight();
            if(bitmap.getHeight() > maxHeight) {
                double ratio = (double) drawable.getIntrinsicHeight() / (double) drawable.getIntrinsicWidth();
                int width =   (int) Math.round(maxHeight / ratio);

                if(Build.VERSION.SDK_INT > 18 ) {
                    bitmap.setHeight((int) maxHeight);
                    bitmap.setWidth(width);
                }
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

}
