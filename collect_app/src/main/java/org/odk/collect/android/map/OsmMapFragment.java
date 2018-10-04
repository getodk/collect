/*
 * Copyright (C) 2018 Nafundi
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

package org.odk.collect.android.map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;

import org.odk.collect.android.R;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsmMapFragment extends Fragment implements MapFragment,
    MapEventsReceiver, LocationListener, LocationClient.LocationClientListener {
    public static final GeoPoint INITIAL_CENTER = new GeoPoint(0.0, -30.0);
    public static final int INITIAL_ZOOM = 2;
    public static final int POINT_ZOOM = 16;

    protected MapView map;
    protected ReadyListener readyListener;
    protected MapFragment.PointListener clickListener;
    protected MapFragment.PointListener longPressListener;
    protected MapFragment.PointListener gpsLocationListener;
    protected MyLocationNewOverlay myLocationOverlay;
    protected LocationClient locationClient;
    protected int nextFeatureId = 1;
    protected Map<Integer, MapFeature> features = new HashMap<>();
    protected AlertDialog gpsErrorDialog;
    protected boolean gpsLocationEnabled;

    @Override public void addTo(@NonNull FragmentActivity activity, int containerId, @Nullable ReadyListener listener) {
        readyListener = listener;
        activity.getSupportFragmentManager()
            .beginTransaction().add(containerId, this).commit();
    }

    // TOOD(ping): This method is only used by MapHelper.  Remove this after
    // MapFragment adds support for selectable basemaps.
    public MapView getMapView() {
        return map;
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater,
        @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.osm_map_layout, container, false);
        map = view.findViewById(R.id.osm_map_view);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(true);
        map.setMinZoomLevel(1);
        map.getController().setCenter(INITIAL_CENTER);
        map.getController().setZoom(INITIAL_ZOOM);
        map.setTilesScaledToDpi(true);
        map.getOverlays().add(new MapEventsOverlay(this));
        myLocationOverlay = new MyLocationNewOverlay(map);
        locationClient = LocationClients.clientForContext(getActivity());
        locationClient.setListener(this);
        if (readyListener != null) {
            new Handler().postDelayed(() -> readyListener.onReady(this), 100);
        }
        return view;
    }

    @Override public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        if (clickListener != null) {
            clickListener.onPoint(fromGeoPoint(geoPoint));
            return true;
        }
        return false;
    }

    @Override public boolean longPressHelper(GeoPoint geoPoint) {
        if (longPressListener != null) {
            longPressListener.onPoint(fromGeoPoint(geoPoint));
            return true;
        }
        return false;
    }

    @Override public @NonNull MapPoint getCenter() {
        return fromGeoPoint(map.getMapCenter());
    }

    @Override public void setCenter(@Nullable MapPoint center) {
        if (center != null) {
            map.getController().setCenter(toGeoPoint(center));
        }
    }

    @Override public double getZoom() {
        return map.getZoomLevel();
    }

    @Override public void zoomToPoint(@Nullable MapPoint center) {
        zoomToPoint(center, POINT_ZOOM);
    }

    @Override public void zoomToPoint(@Nullable MapPoint center, double zoom) {
        if (center != null) {
            // setCenter() must be done last; setZoom() does not preserve the center.
            map.getController().setZoom((int) Math.round(zoom));
            map.getController().setCenter(toGeoPoint(center));
        }
    }

    @Override public void zoomToBoundingBox(Iterable<MapPoint> points, double scaleFactor) {
        if (points != null) {
            int count = 0;
            List<GeoPoint> geoPoints = new ArrayList<>();
            MapPoint lastPoint = null;
            for (MapPoint point : points) {
                lastPoint = point;
                geoPoints.add(toGeoPoint(point));
                count++;
            }
            if (count == 1) {
                zoomToPoint(lastPoint);
            } else if (count > 1) {
                // TODO(ping): Find a better solution.
                // zoomToBoundingBox sometimes fails to zoom correctly, either
                // zooming by the correct amount but leaving the bounding box
                // off-center, or centering correctly but not zooming in enough.
                // Adding a 100-ms delay avoids the problem most of the time, but
                // not always; it's here because the old GeoShapeOsmMapActivity
                // did it, not because it's known to be the best solution.
                final BoundingBox box = BoundingBox.fromGeoPoints(geoPoints)
                    .increaseByScale((float) (1 / scaleFactor));
                new Handler().postDelayed(() -> map.zoomToBoundingBox(box, false), 100);
            }
        }
    }

    @Override public int addDraggablePoly(@NonNull Iterable<MapPoint> points, boolean closedPolygon) {
        int featureId = nextFeatureId++;
        features.put(featureId, new DraggablePoly(map, points, closedPolygon));
        return featureId;
    }

    @Override public void appendPointToPoly(int featureId, @NonNull MapPoint point) {
        MapFeature feature = features.get(featureId);
        if (feature != null && feature instanceof DraggablePoly) {
            ((DraggablePoly) feature).addPoint(point);
        }
    }

    @Override public @NonNull List<MapPoint> getPointsOfPoly(int featureId) {
        MapFeature feature = features.get(featureId);
        if (feature instanceof DraggablePoly) {
            return ((DraggablePoly) feature).getPoints();
        }
        return new ArrayList<>();
    }

    @Override public void removeFeature(int featureId) {
        MapFeature feature = features.get(featureId);
        if (feature != null) {
            feature.dispose();
        }
    }

    @Override public void clearFeatures() {
        map.getOverlays().clear();
        map.getOverlays().add(new MapEventsOverlay(this));
        map.getOverlays().add(myLocationOverlay);
        map.invalidate();
        features.clear();
    }

    @Override public void setClickListener(@Nullable PointListener listener) {
        clickListener = listener;
    }

    @Override public void setLongPressListener(@Nullable PointListener listener) {
        longPressListener = listener;
    }

    @Override public void setGpsLocationListener(@Nullable PointListener listener) {
        gpsLocationListener = listener;
    }

    @Override public void runOnGpsLocationReady(@NonNull ReadyListener listener) {
        myLocationOverlay.runOnFirstFix(() -> getActivity().runOnUiThread(() -> listener.onReady(this)));
    }

    @Override public void setGpsLocationEnabled(boolean enable) {
        if (enable != gpsLocationEnabled) {
            gpsLocationEnabled = enable;
            if (locationClient == null) {
                locationClient = LocationClients.clientForContext(getActivity());
                locationClient.setListener(this);
            }
            if (gpsLocationEnabled) {
                LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    map.getOverlays().add(myLocationOverlay);
                    myLocationOverlay.setEnabled(true);
                    myLocationOverlay.enableMyLocation();
                    locationClient.start();
                } else {
                    showGpsDisabledAlert();
                }
            } else {
                locationClient.stop();
                myLocationOverlay.setEnabled(false);
                myLocationOverlay.disableFollowLocation();
                myLocationOverlay.disableMyLocation();
            }
        }
    }

    @Override public @Nullable MapPoint getGpsLocation() {
        return fromLocation(myLocationOverlay);
    }

    @Override public void onLocationChanged(Location location) {
        if (gpsLocationListener != null) {
            MapPoint point = fromLocation(myLocationOverlay);
            if (point != null) {
                gpsLocationListener.onPoint(point);
            }
        }
    }

    protected void showGpsDisabledAlert() {
        gpsErrorDialog = new AlertDialog.Builder(getContext())
            .setMessage(getString(R.string.gps_enable_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.enable_gps),
                (dialog, id) -> startActivityForResult(
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0))
            .setNegativeButton(getString(R.string.cancel),
                (dialog, id) -> dialog.cancel())
            .create();
        gpsErrorDialog.show();
    }

    @Override public void onClientStart() {
        locationClient.requestLocationUpdates(this);
    }

    @Override public void onClientStartFailure() {
        showGpsDisabledAlert();
    }

    @Override public void onClientStop() { }

    @VisibleForTesting public AlertDialog getGpsErrorDialog() {
        return gpsErrorDialog;
    }

    protected static @Nullable MapPoint fromLocation(@NonNull MyLocationNewOverlay overlay) {
        GeoPoint geoPoint = overlay.getMyLocation();
        if (geoPoint == null) {
            return null;
        }
        return fromGeoPoint(geoPoint, overlay.getLastFix().getAccuracy());
    }

    protected static @NonNull MapPoint fromGeoPoint(@NonNull IGeoPoint geoPoint) {
        return new MapPoint(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    protected static @NonNull MapPoint fromGeoPoint(@NonNull GeoPoint geoPoint) {
        return new MapPoint(geoPoint.getLatitude(), geoPoint.getLongitude(), geoPoint.getAltitude());
    }

    protected static @NonNull MapPoint fromGeoPoint(@NonNull GeoPoint geoPoint, double sd) {
        return new MapPoint(geoPoint.getLatitude(), geoPoint.getLongitude(), geoPoint.getAltitude(), sd);
    }

    protected static @NonNull GeoPoint toGeoPoint(@NonNull MapPoint point) {
        return new GeoPoint(point.lat, point.lon, point.alt);
    }

    /**
     * A MapFeature is a physical feature on a map, such as a point, a road,
     * a building, a region, etc.  It is presented to the user as one editable
     * object, though its appearance may be constructed from multiple overlays
     * (e.g. geometric elements, handles for manipulation, etc.).
     */
    interface MapFeature {
        /** Updates the feature's geometry after any UI handles have moved. */
        void update();

        /** Removes the feature from the map, leaving it no longer usable. */
        void dispose();
    }

    /** A polyline or polygon that can be manipulated by dragging markers at its vertices. */
    protected static class DraggablePoly implements MapFeature, Marker.OnMarkerClickListener, Marker.OnMarkerDragListener {
        final MapView map;
        final List<Marker> markers = new ArrayList<>();
        final Polyline polyline;
        final boolean closedPolygon;
        public static final int STROKE_WIDTH = 5;

        public DraggablePoly(MapView map, Iterable<MapPoint> points, boolean closedPolygon) {
            this.map = map;
            this.closedPolygon = closedPolygon;
            polyline = new Polyline();
            polyline.setColor(Color.RED);
            Paint paint = polyline.getPaint();
            paint.setStrokeWidth(STROKE_WIDTH);
            map.getOverlays().add(polyline);
            for (MapPoint point : points) {
                addMarker(point);
            }
            update();
        }

        public void update() {
            List<GeoPoint> geoPoints = new ArrayList<>();
            for (Marker marker : markers) {
                geoPoints.add(marker.getPosition());
            }
            if (closedPolygon && !geoPoints.isEmpty()) {
                geoPoints.add(geoPoints.get(0));
            }
            polyline.setPoints(geoPoints);
            map.invalidate();
        }

        public void dispose() {
            for (Marker marker : markers) {
                map.getOverlays().remove(marker);
            }
            markers.clear();
            update();
        }

        public List<MapPoint> getPoints() {
            List<MapPoint> points = new ArrayList<>();
            for (Marker marker : markers) {
                points.add(fromGeoPoint(
                    marker.getPosition(), Double.valueOf(marker.getSubDescription())));
            }
            return points;
        }

        public void addPoint(MapPoint point) {
            addMarker(point);
            update();
        }

        protected void addMarker(MapPoint point) {
            // A Marker's position is a GeoPoint with latitude, longitude, and
            // altitude fields.  We need to store the standard deviation value
            // somewhere, so it goes in the marker's sub-description field.
            Marker marker = new Marker(map);
            marker.setPosition(toGeoPoint(point));
            marker.setSubDescription(Double.toString(point.sd));
            marker.setDraggable(true);
            marker.setIcon(ContextCompat.getDrawable(map.getContext(), R.drawable.ic_place_black));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setOnMarkerClickListener(this);
            marker.setOnMarkerDragListener(this);
            map.getOverlays().add(marker);
            markers.add(marker);
        }

        @Override public void onMarkerDragStart(Marker marker) {
        }

        @Override public void onMarkerDragEnd(Marker marker) {
            update();
        }

        @Override public void onMarkerDrag(Marker marker) {
            // When a marker is manually dragged, the position is no longer
            // obtained from a GPS reading, so the standard deviation field
            // is no longer meaningful; reset it to zero.
            marker.setSubDescription("0");
            update();
        }

        @Override public boolean onMarkerClick(Marker marker, MapView map) {
            // Prevent the text bubble from appearing when a marker is clicked.
            return false;
        }
    }

    @VisibleForTesting public boolean isGpsErrorDialogShowing() {
        return gpsErrorDialog != null && gpsErrorDialog.isShowing();
    }
}
