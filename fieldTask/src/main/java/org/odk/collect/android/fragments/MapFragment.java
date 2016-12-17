package org.odk.collect.android.fragments;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/*
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.Marker;

import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
*/
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.PathOverlay;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainListActivity;
import org.odk.collect.android.activities.MainTabsActivity;
import org.odk.collect.android.loaders.MapLocationObserver;
import org.odk.collect.android.receivers.LocationChangedReceiver;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.odk.collect.android.loaders.MapDataLoader;
import org.odk.collect.android.loaders.MapEntry;
import org.odk.collect.android.loaders.PointEntry;
import org.odk.collect.android.loaders.TaskEntry;

import static org.odk.collect.android.R.drawable;

public class MapFragment extends Fragment implements LoaderManager.LoaderCallbacks<MapEntry>
{

    private static final String TAG = "MapFragment";
    PathOverlay po = null;
    private PointEntry lastPathPoint;

    ItemizedIconOverlay markerOverlay = null;
    //ArrayList<Marker> markers = null;
    //HashMap<Marker, Integer> markerMap = null;
    private double tasksNorth;
    private double tasksSouth;
    private double tasksEast;
    private double tasksWest;

    /*
    Marker userLocationMarker = null;
    Icon userLocationIcon = null;
    Icon accepted = null;
    Icon repeat = null;
    Icon rejected = null;
    Icon complete = null;
    Icon submitted = null;
    Icon triggered = null;
    Icon triggered_repeat = null;
*/
    private static MainTabsActivity mainTabsActivity;
    private static MapFragment thisActvity;
    //private MapView mv;
    //private MapboxMap map;

    private static final int MAP_LOADER_ID = 2;

    public void setTabsActivity(MainTabsActivity activity) {
        mainTabsActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        thisActvity = this;
        getLoaderManager().initLoader(MAP_LOADER_ID, null, thisActvity);       // Get the task locations
    /*
        View view = inflater.inflate(R.layout.ft_map_fragment, container, false);
        mv = (MapView) view.findViewById(R.id.mapview);
        thisActvity = this;
        mv.onCreate(savedInstanceState);
        mv.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                map = mapboxMap;

                // Customize map with markers, polylines, etc.
                IconFactory iconFactory = IconFactory.getInstance(mainTabsActivity);

                userLocationIcon = iconFactory.fromDrawable(ContextCompat.getDrawable(mainTabsActivity, R.drawable.ic_userlocation));
                accepted = iconFactory.fromDrawable(ContextCompat.getDrawable(mainTabsActivity, R.drawable.ic_task_open));
                repeat = iconFactory.fromDrawable(ContextCompat.getDrawable(mainTabsActivity, drawable.ic_task_repeat));
                rejected = iconFactory.fromDrawable(ContextCompat.getDrawable(mainTabsActivity, drawable.ic_task_reject));
                complete = iconFactory.fromDrawable(ContextCompat.getDrawable(mainTabsActivity, drawable.ic_task_done));
                submitted = iconFactory.fromDrawable(ContextCompat.getDrawable(mainTabsActivity, drawable.ic_task_submitted));
                triggered = iconFactory.fromDrawable(ContextCompat.getDrawable(mainTabsActivity, drawable.ic_task_triggered));
                triggered_repeat = iconFactory.fromDrawable(ContextCompat.getDrawable(mainTabsActivity, drawable.ic_task_triggered_repeat));

                //replaceMapView("mapquest");

                getLoaderManager().initLoader(MAP_LOADER_ID, null, thisActvity);       // Get the task locations
                map.setMyLocationEnabled(true);
            }
        });


        mv.setVisibility(View.VISIBLE);

        return view;
        */
        return null;
    }

    @Override
    public void onLoadFinished(Loader<MapEntry> loader, MapEntry data) {
        Log.i(TAG, "######### Load Finished");
        mainTabsActivity.setLocationTriggers(data.tasks, true);
        showTasks(data.tasks);
        showPoints(data.points);
        //zoomToData(false);
    }

    @Override
    public void onLoaderReset(Loader<MapEntry> loader) {
        clearTasks();
    }

    @Override
    public Loader<MapEntry> onCreateLoader(int id, Bundle args) {
        return new MapDataLoader(getActivity());
    }

    protected void replaceMapView(String layer) {
        /*
        ITileLayer source;
        BoundingBox box;
        if (layer.toLowerCase().endsWith("mbtiles")) {
            TileLayer mbTileLayer = new MBTilesLayer(getActivity(), layer);
            //            mv.setTileSource(mbTileLayer);
            mv.setTileSource(new ITileLayer[] {
                mbTileLayer, new WebSourceTileLayer("mapquest",
                    "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png").setName(
                        "MapQuest Open Aerial")
                        .setAttribution("Tiles courtesy of MapQuest and OpenStreetMap contributors.")
                .setMinimumZoomLevel(1)
                .setMaximumZoomLevel(18)
            });
            box = mbTileLayer.getBoundingBox();
        } else {
            if (layer.equalsIgnoreCase("OpenStreetMap")) {
                source = new WebSourceTileLayer("openstreetmap",
                        "http://tile.openstreetmap.org/{z}/{x}/{y}.png").setName("OpenStreetMap")
                    .setAttribution("© OpenStreetMap Contributors")
                    .setMinimumZoomLevel(1)
                    .setMaximumZoomLevel(18);
            } else if (layer.equalsIgnoreCase("OpenSeaMap")) {
                source = new WebSourceTileLayer("openstreetmap",
                        "http://tile.openstreetmap.org/seamark/{z}/{x}/{y}.png").setName(
                            "OpenStreetMap")
                            .setAttribution("© OpenStreetMap Contributors")
                            .setMinimumZoomLevel(1)
                            .setMaximumZoomLevel(18);
            } else if (layer.equalsIgnoreCase("mapquest")) {
                source = new WebSourceTileLayer("mapquest",
                        "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png").setName(
                            "MapQuest Open Aerial")
                            .setAttribution(
                                    "Tiles courtesy of MapQuest and OpenStreetMap contributors.")
                            .setMinimumZoomLevel(1)
                            .setMaximumZoomLevel(18);
            } else {
                source = new MapboxTileLayer(layer);
            }
            mv.setTileSource(source);
            box = source.getBoundingBox();
        }
        //        mv.setScrollableAreaLimit(mv.getTileProvider().getBoundingBox());
        mv.setScrollableAreaLimit(box);
        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        mv.setCenter(mv.getTileProvider().getCenterCoordinate());
        mv.setZoom(0);
        Log.d(TAG, "zoomToBoundingBox " + box.toString());
        //        mv.zoomToBoundingBox(box);
        */
    }


    /**
     * Method to show settings  in alert dialog
     * On pressing Settings button will launch Settings Options - GPS
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getActivity().startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void showTasks(List<TaskEntry> data) {

/*
        clearTasks();   // remove existing markers

        // Update markers
        markers = new ArrayList<Marker> ();
        markerMap = new HashMap<Marker, Integer> ();

        // Add the tasks to the marker array and to the map
        int index = 0;
        for(TaskEntry t : data) {
            if(t.type.equals("task")) {
                LatLng ll = getTaskCoords(t);
                if (ll != null) {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(ll)
                            .title(getString(R.string.smap_task))
                            .snippet(t.displayName);

                    markerOptions.icon(getIcon(t.taskStatus, t.repeat, t.locationTrigger != null));

                    Marker m = new Marker(markerOptions);

                    markers.add(m);
                    markerMap.put(m, index);
                    map.addMarker(markerOptions);
                }
            }
            index++;
        }

*/
    }


    private void clearTasks() {
        /*
        if(markers != null && markers.size() > 0) {
            for (int i = 0; i < markers.size(); i++) {
                map.removeMarker(markers.get(i));
            }
        }
        */
    }

    private void showPoints(List<PointEntry> data) {
        if(po == null) {
            Log.i(TAG, "====== po null");
            addPathOverlay();
        } else {
            Log.i(TAG, "====== Removed all points");
            //po.removeAllPoints();
            //mv.removeOverlay(po);
            addPathOverlay();
        }


        for(int i = 0; i < data.size(); i++) {
            //po.addPoint(data.get(i).lat, data.get(i).lon);
        }
        if(data.size() > 0) {
            lastPathPoint = new PointEntry();
            PointEntry lastPoint = data.get(data.size() - 1);
            lastPathPoint.lat = lastPoint.lat;
            lastPathPoint.lon = lastPoint.lon;
        }
    }


/*
    private void updatePath(LatLng point) {
        if(po == null) {
            addPathOverlay();
        }

        //po.addPoint(point);

    }
    */

    private void addPathOverlay() {
        Paint linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(5);

        //po = new PathOverlay().setPaint(linePaint);
        //mv.getOverlays().add(po);
    }

    /*
    private void zoomToData(boolean userLocationChanged) {

        Log.i(TAG, "zoomToData");

        boolean userOutsideBoundingBox = false;
        double north = tasksNorth;
        double south = tasksSouth;
        double east = tasksEast;
        double west = tasksWest;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int locationCount = 0;
        LatLng aPosition = null;        // Used to zoom to a single location of there are no bounds

        if(markers.size() > 0) {
            for(int i = 0; i < markers.size(); i++) {
                aPosition = markers.get(i).getPosition();
                builder.include(aPosition);
                locationCount++;
            }
        }

        // Add current location to bounding box
        if(userLocationMarker != null) {
            aPosition = userLocationMarker.getPosition();
            builder.include(aPosition);
            locationCount++;
        }

        if(locationCount == 1) {

            CameraPosition position = new CameraPosition.Builder()
                    .target(aPosition) // Sets the new camera position
                    .zoom(17) // Sets the zoom
                    .build();

            map.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 7000);

        } else if(locationCount > 1){
            LatLngBounds latLngBounds = builder.build();
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10), 7000);
        }
            /*
            LatLngBounds latLngBounds = new LatLngBounds.Builder().
                    include(userLocationMarker.getPosition()).build();

            map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10), 7000);
            */

            // double lat = userLocationMarker.getPosition().getLatitude();
            // double lon = userLocationMarker.getPosition().getLongitude();

            /*
            if(lat > north) {
                north = lat;
            }
            if(lat < south) {
                south = lat;
            }
            if(lon > east) {
                east = lon;
            }
            if(lon < west) {
                west = lon;
            }
            */

            /*
            if(userLocationChanged) {
                Rect viewableBox = mv.getClipBounds();
                if(viewableBox != null) {
                    if(!viewableBox.contains(userLocationMarker.getPosition()))
                    if (lat > viewableBox.getLatNorth() ||
                            lat < viewableBox.getLatSouth() ||
                            lon > viewableBox.getLonEast() ||
                            lon < viewableBox.getLonWest()
                            ) {
                        userOutsideBoundingBox = true;
                    }
                } else {
                    userOutsideBoundingBox = true;      // User location being set on resume of activity
                }
            }
            */



        // Add last path point to bounding box
        /*
        if(lastPathPoint != null) {
            double lat = lastPathPoint.lat;
            double lon = lastPathPoint.lon;
            if(lat > north) {
                north = lat;
            }
            if(lat < south) {
                south = lat;
            }
            if(lon > east) {
                east = lon;
            }
            if(lon < west) {
                west = lon;
            }

        }

        // Make sure bounding box is not a point
        if(north == south) {
            north += 0.01;
            south -= 0.01;
        }
        if(east == west) {
            east += 0.01;
            west -= 0.01;
        }
        */

        /*
         * Zoom to the new bounding box only if the task list has changed or the user is outside of the current
         *  viewable area
         *
        if(north > south && east > west) {
            if(!userLocationChanged || userOutsideBoundingBox) {
                BoundingBox bb = new BoundingBox(north, east, south, west);
                mv.zoomToBoundingBox(bb, true, true, true, true);
                mv.fit
            }
        }



    }
*/

    /*
     * Get the colour to represent the passed in task status
     *
    private Icon getIcon(String status, boolean isRepeat, boolean hasTrigger) {

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
*/
    /*
     * Get the coordinates of the task and update the bounding box
     *
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
*/
/*
    ItemizedIconOverlay.OnItemGestureListener<Marker> onItemGestureListener
            = new ItemizedIconOverlay.OnItemGestureListener<Marker>(){

        @Override
        public boolean onItemLongPress(int arg0, Marker item) {
            return processTouch(item);
        }

        @Override
        public boolean onItemSingleTapUp(int index, Marker item) {
            return processTouch(item);
        }

        public boolean processTouch(Marker item) {

            Integer iPos = markerMap.get(item);

            Log.i(TAG, "process Touch");
            if(iPos != null) {
                /*
                int position = iPos;
                List<TaskEntry> mapTasks = mainTabsActivity.getMapTasks();
                TaskEntry entry = mapTasks.get(position);

                if(entry.locationTrigger != null) {
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.smap_must_start_from_nfc),
                            Toast.LENGTH_SHORT).show();
                } else {
                    mainTabsActivity.completeTask(entry);
                }


            }

            return true;
        }

    };
*/
    /*
 * Update the user location
 *
    public void setUserLocation(Location location, boolean recordLocation) {
        Log.i(TAG, "setUserLocation()");

        if(location != null && map != null) {
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());

            if (userLocationMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(point)
                        .title("you")
                        .snippet("your location")
                        .icon(userLocationIcon);

                userLocationMarker = new Marker(markerOptions);
                //map.addMarker(markerOptions);

            } else {
                // userLocationMarker.setPoint(point);
                //userLocationMarker.updateDrawingPosition();
                userLocationMarker.setPosition(point);

            }
            if (recordLocation) {
                // updatePath(point);  TODO
            }
            zoomToData(true);
        }
    }
*/
/*
    public void resumeMap() {
        mv.onResume();
    }

    public void pauseMap() {
        mv.onPause();
    }

    public void lowMemoryMap() {

        mv.onLowMemory();
    }

    public void destroyMap() {

        mv.onDestroy();
    }

    public void onSaveInstanceState(Bundle outState) {
        mv.onSaveInstanceState(outState);
    }
    */
}
