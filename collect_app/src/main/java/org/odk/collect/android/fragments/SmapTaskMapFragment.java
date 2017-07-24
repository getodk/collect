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
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.SmapMain;
import org.odk.collect.android.adapters.TaskListArrayAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.loaders.TaskLoader;
import org.odk.collect.android.preferences.AboutPreferencesActivity;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.tasks.DeleteInstancesTask;
import org.odk.collect.android.tasks.InstanceSyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_DATE_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_NAME_DESC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_ASC;
import static org.odk.collect.android.utilities.ApplicationConstants.SortingOrder.BY_STATUS_DESC;

/**
 * Responsible for displaying tasks on the main fieldTask screen
 */
public class SmapTaskMapFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<TaskEntry>>, OnMapReadyCallback {

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

    private GoogleMap mMap;
    private Polyline mPath;
    private MapHelper mHelper;
    private Button layers_button;
    private Button location_button;

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

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {

        mAdapter = new TaskListArrayAdapter(getActivity());
        getLoaderManager().initLoader(TASK_LOADER_ID, null, this);

        super.onViewCreated(rootView, savedInstanceState);
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle bundle) {
        super.onViewStateRestored(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_launcher);
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
    public Loader<List<TaskEntry>> onCreateLoader(int id, Bundle args) {
        return new TaskLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<TaskEntry>> loader, List<TaskEntry> data) {

        for (TaskEntry te : data) {
            Timber.i("Form: " + te.displayName);
        }

        // smap TODO enable when adapter added
        mAdapter.setData(data);

        // TODO Smap
        //tabsActivity.setLocationTriggers(data, false);      // NFC and geofence triggers

        // smap todo
        //if (isResumed()) {
        //    setListShown(true);
        //} else {
        //    setListShownNoAnimation(true);
        //}
    }

    @Override
    public void onLoaderReset(Loader<List<TaskEntry>> loader) {
        mAdapter.setData(null);
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
                Intent aboutIntent = new Intent(getActivity(), AboutPreferencesActivity.class);
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
            case R.id.menu_gettasks:
                ((SmapMain) getActivity()).processGetTask();
                return true;


            /*
            case MENU_ENTERDATA:
                processEnterData();
                return true;
            case MENU_GETFORMS:
                processGetForms();
                return true;
            case MENU_SENDDATA:
                processSendData();
                return true;
                */
            /*
            case MENU_MANAGEFILES:
                processManageFiles();
                return true;
                */

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


    private void hideSearchBox() {
        inputSearch.setText("");
        searchBoxLayout.setVisibility(View.GONE);
        Collect.getInstance().hideKeyboard(inputSearch);
    }

    private void showSearchBox() {
        searchBoxLayout.setVisibility(View.VISIBLE);
        Collect.getInstance().showKeyboard(inputSearch);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mHelper = new MapHelper(getActivity(),mMap);
        mHelper.setBasemap();

        location_button = (Button) getActivity().findViewById(R.id.show_location);
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


        layers_button = (Button)getActivity().findViewById(R.id.layers);
        layers_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mHelper.showLayersDialog(getActivity());

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
                    Toast.makeText(getActivity(), "marker selected: " + selMarker.getTitle(), Toast.LENGTH_SHORT).show();

                    Integer iPos = markerMap.get(selMarker);
                    if(iPos != null) {

                        int position = iPos;
                        List<TaskEntry> mapTasks = ((SmapMain) getActivity()).getMapTasks();
                        TaskEntry entry = mapTasks.get(position);

                        if(entry.locationTrigger != null) {
                            Toast.makeText(
                                    getActivity(),
                                    getString(R.string.smap_must_start_from_nfc),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            ((SmapMain) getActivity()).completeTask(entry);
                        }


                    }
                }

            }
        });
        getLoaderManager().initLoader(MAP_LOADER_ID, null, this);       // Get the task locations
    }



}
