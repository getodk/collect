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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.AboutActivity;
import org.odk.collect.android.activities.SmapMain;
import org.odk.collect.android.activities.viewmodels.SurveyDataViewModel;
import org.odk.collect.android.geo.MapFragment;
import org.odk.collect.android.geo.MapPoint;
import org.odk.collect.android.geo.MapProvider;
import org.odk.collect.android.geo.MapTabMapProvider;
import org.odk.collect.android.geo.TaskMapMarker;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.loaders.PointEntry;
import org.odk.collect.android.loaders.SurveyData;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.Utilities;
import org.odk.collect.android.views.CustomMarker;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Responsible for displaying tasks on the main fieldTask screen
 */
public class SmapTaskMapFragment extends Fragment {

    private final MapProvider mapProvider = new MapTabMapProvider();

    private SharedPreferences adminPreferences;

    Map<Integer, TaskMapMarker> markersMap = new HashMap<>();
    private int polyFeatureId = -1;

    Drawable complete = null;
    Drawable accepted = null;
    Drawable late = null;
    Drawable repeat = null;
    Drawable rejected = null;
    Drawable newtask = null;
    Drawable submitted = null;
    Drawable triggered = null;
    Drawable triggered_repeat = null;

    private double tasksNorth;
    private double tasksSouth;
    private double tasksEast;
    private double tasksWest;

    private SurveyDataViewModel viewModel;

    @Inject
    PermissionsProvider permissionsProvider;

    private MapFragment mapFragment;

    public static SmapTaskMapFragment newInstance() {
        return new SmapTaskMapFragment();
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
        View rootView = inflater.inflate(R.layout.ft_map_layout, container, false);
        Timber.i("######## onCreateView");

        MapFragment mapFragment = mapProvider.createMapFragment(requireContext());
        mapFragment.addTo(getChildFragmentManager(), R.id.map_container, this::initMap, () -> {
        });

        setHasOptionsMenu(true);
        return rootView;
    }

    public SurveyDataViewModel getViewMode() {
        return ((SmapMain) getActivity()).getViewModel();
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        Timber.i("######## onViewCreated");
        super.onViewCreated(rootView, savedInstanceState);

        viewModel = getViewMode();

        complete = ResourcesCompat.getDrawable(getResources(), R.drawable.form_state_finalized_circle, getContext().getTheme());
        accepted = ResourcesCompat.getDrawable(getResources(), R.drawable.form_state_saved_circle, getContext().getTheme());
        late = ResourcesCompat.getDrawable(getResources(), R.drawable.form_state_late, getContext().getTheme());
        repeat = ResourcesCompat.getDrawable(getResources(), R.drawable.form_state_repeat, getContext().getTheme());
        rejected = ResourcesCompat.getDrawable(getResources(), R.drawable.form_state_rejected, getContext().getTheme());
        newtask = ResourcesCompat.getDrawable(getResources(), R.drawable.form_state_new, getContext().getTheme());
        submitted = ResourcesCompat.getDrawable(getResources(), R.drawable.form_state_submitted_circle, getContext().getTheme());
        triggered = ResourcesCompat.getDrawable(getResources(), R.drawable.form_state_triggered, getContext().getTheme());
        triggered_repeat = ResourcesCompat.getDrawable(getResources(), R.drawable.form_state_triggered, getContext().getTheme());
    }

    private void initMap(MapFragment mapFragment) {
        this.mapFragment = mapFragment;

        viewModel.getSurveyData().observe(getViewLifecycleOwner(), surveyData -> {
            Timber.i("-------------------------------------- Task Map Fragment got Data ");
            setData(surveyData);
        });

        permissionsProvider.requestLocationPermissions((Activity) getContext(), new PermissionListener() {
            @Override
            public void granted() {
                ((SmapMain) getActivity()).startLocationService();
                mapFragment.setGpsLocationEnabled(true);
            }

            @Override
            public void denied() {
            }
        });

        mapFragment.setLongPressListener(point -> {
            double minDistance = 1000;
            TaskMapMarker selMarker = null;
            if (markersMap != null) {
                for (Map.Entry<Integer, TaskMapMarker> entry : markersMap.entrySet()) {
                    TaskMapMarker taskMapMarker = entry.getValue();
                    MapPoint mapPoint = taskMapMarker.getMapPoint();

                    double roughDistance = Math.sqrt(
                            Math.pow(mapPoint.lat - point.lat, 2)
                                    + Math.pow(mapPoint.lon - point.lon, 2)
                    );
                    if (roughDistance < minDistance) {
                        minDistance = roughDistance;
                        selMarker = taskMapMarker;
                    }
                }
            }
            if (selMarker != null) {
                TaskEntry entry = selMarker.getTask();

                Toast.makeText(getActivity(), "marker selected: " + entry.name, Toast.LENGTH_LONG).show();

                if (entry.locationTrigger != null) {
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.smap_must_start_from_nfc),
                            Toast.LENGTH_LONG).show();
                } else {
                    ((SmapMain) getActivity()).completeTask(entry, false);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle b) {
        adminPreferences = getActivity().getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

        super.onActivityCreated(b);
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

    public void setData(SurveyData data) {
        if (data != null) {
            showTasks(data.tasks);
            showPoints(data.points);
        } else {
            clearTasks();
        }
    }

    @Override
    public void onResume() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_nav);
        viewModel.loadData();   // Update the user trail display with latest points
        super.onResume();
    }

    private void clearTasks() {
        for (int markerId : markersMap.keySet()) {
            mapFragment.removeFeature(markerId);
        }
    }

    private void showTasks(List<TaskEntry> data) {
        if (mapFragment != null) {

            clearTasks();   // remove existing markers

            // Add the tasks to the marker array and to the map
            for (TaskEntry t : data) {
                if (t.type.equals("task")) {
                    LatLng ll = getTaskCoords(t);
                    if (ll != null) {
                        Drawable icon = getIcon(t.taskStatus, t.repeat, t.locationTrigger != null, t.taskFinish);

                        MapPoint point = new MapPoint(ll.latitude, ll.longitude);
                        TaskMapMarker marker = new TaskMapMarker(point, t);

                        CustomMarker customMarkerView = new CustomMarker(
                                getContext(),
                                marker.getTask().name,
                                marker.getAddressText(),
                                icon
                        );

                        int markerId = mapFragment.addMarker(point, customMarkerView);
                        markersMap.put(markerId, marker);
                    }
                }
            }
        }

    }

    private void showPoints(List<PointEntry> data) {
        if (mapFragment != null) {
            if (polyFeatureId != -1) {
                mapFragment.removeFeature(polyFeatureId);
            }

            List<MapPoint> mapPoints = new ArrayList<>();
            for (int i = data.size() - 1; i >= 0; i--) {
                PointEntry p = data.get(i);
                mapPoints.add(new MapPoint(p.lat, p.lon));
            }
            polyFeatureId = mapFragment.addDraggablePoly(mapPoints, false, null);
        }
    }

    /*
     * Get the coordinates of the task and update the bounding box
     */
    private LatLng getTaskCoords(TaskEntry t) {

        double lat;
        double lon;
        LatLng locn = null;

        if ((t.actLat == 0.0) && (t.actLon == 0.0)) {
            lat = t.schedLat;       // Scheduled coordinates of task
            lon = t.schedLon;
        } else {
            lat = t.actLat;         // Actual coordinates of task
            lon = t.actLon;
        }

        if (lat != 0.0 && lon != 0.0) {
            // Update bounding box
            if (lat > tasksNorth) {
                tasksNorth = lat;
            }
            if (lat < tasksSouth) {
                tasksSouth = lat;
            }
            if (lon > tasksEast) {
                tasksEast = lon;
            }
            if (lat < tasksWest) {
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
    private Drawable getIcon(String status, boolean isRepeat, boolean hasTrigger, long taskFinish) {

        switch (status) {
            case Utilities.STATUS_T_REJECTED:
            case Utilities.STATUS_T_CANCELLED:
                return rejected;
            case Utilities.STATUS_T_ACCEPTED:
                if (hasTrigger && !isRepeat) {
                    return triggered;
                } else if (hasTrigger) {
                    return triggered_repeat;
                } else if (isRepeat) {
                    return repeat;
                } else if (taskFinish != 0 && taskFinish < (new Date()).getTime()) {
                    return late;
                } else {
                    return accepted;
                }
            case Utilities.STATUS_T_COMPLETE:
                return complete;
            case Utilities.STATUS_T_SUBMITTED:
                return submitted;
            case Utilities.STATUS_T_NEW:
                return newtask;
            default:
                Timber.i("Unknown task status: %s", status);
                return accepted;
        }
    }

    public void locateTask(TaskEntry task) {
        MapPoint point = new MapPoint(task.schedLat, task.schedLon);
        mapFragment.zoomToPoint(point, true);
    }

}
