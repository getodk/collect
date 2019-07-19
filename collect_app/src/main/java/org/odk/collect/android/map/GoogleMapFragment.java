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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.provider.Settings;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import org.odk.collect.android.R;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.spatial.GoogleMapsMapBoxOfflineTileProvider;
import org.odk.collect.android.utilities.IconUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import timber.log.Timber;

public class GoogleMapFragment extends SupportMapFragment implements
    MapFragment, LocationListener, LocationClient.LocationClientListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
    GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {
    public static final LatLng INITIAL_CENTER = new LatLng(0, -30);
    public static final float INITIAL_ZOOM = 2;
    public static final float POINT_ZOOM = 16;

    protected GoogleMap map;
    protected Marker locationCrosshairs;
    protected Circle accuracyCircle;
    protected List<ReadyListener> gpsLocationReadyListeners = new ArrayList<>();
    protected PointListener clickListener;
    protected PointListener longPressListener;
    protected PointListener gpsLocationListener;
    protected FeatureListener dragEndListener;
    protected LocationClient locationClient;
    protected MapPoint lastLocationFix;
    protected String lastLocationProvider;
    protected int nextFeatureId = 1;
    protected Map<Integer, MapFeature> features = new HashMap<>();
    protected AlertDialog gpsErrorDialog;
    protected boolean gpsLocationEnabled;
    protected final int mapType;
    protected final File referenceLayer;
    protected TileOverlay tileOverlay;

    // During Robolectric tests, Google Play Services is unavailable; sadly, the
    // "map" field will be null and many operations will need to be stubbed out.
    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "This flag is exposed for Robolectric tests to set")
    @VisibleForTesting public static boolean testMode;

    public GoogleMapFragment(int mapType, File referenceLayer) {
        this.mapType = mapType;
        this.referenceLayer = referenceLayer;
    }

    @Override public Fragment getFragment() {
        return this;
    }

    @SuppressLint("MissingPermission") // Permission checks for location services handled in widgets
    @Override public void addTo(@NonNull FragmentActivity activity, int containerId, @Nullable ReadyListener listener) {
        // If the containing activity is being re-created upon screen rotation,
        // the FragmentManager will have also re-created a copy of the previous
        // OsmMapFragment.  We don't want these useless copies of old fragments
        // to linger, so the following line calls .replace() instead of .add().
        activity.getSupportFragmentManager()
            .beginTransaction().replace(containerId, this).commitNow();
        getMapAsync((GoogleMap map) -> {
            if (map == null) {
                ToastUtils.showShortToast(R.string.google_play_services_error_occured);
                return;
            }
            this.map = map;
            setReferenceLayer(referenceLayer);
            map.setMapType(mapType);
            map.setOnMapClickListener(this);
            map.setOnMapLongClickListener(this);
            map.setOnMarkerClickListener(this);
            map.setOnMarkerDragListener(this);
            map.getUiSettings().setCompassEnabled(true);
            // Don't show the blue dot on the map; we'll draw crosshairs instead.
            map.setMyLocationEnabled(false);
            map.setMinZoomPreference(1);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(INITIAL_CENTER, INITIAL_ZOOM));
            if (listener != null) {
                listener.onReady(this);
            }
        });

        // In Robolectric tests, getMapAsync() never gets around to calling its
        // callback; we have to invoke the ready listener directly.
        if (testMode) {
            listener.onReady(this);
        }
    }

    public void setReferenceLayer(File file) {
        if (tileOverlay != null) {
            tileOverlay.remove();
            tileOverlay = null;
        }
        if (file != null) {
            tileOverlay = this.map.addTileOverlay(new TileOverlayOptions().tileProvider(
                new GoogleMapsMapBoxOfflineTileProvider(file)
            ));
        }
    }

    // TOOD(ping): This method is only used by MapHelper.  Remove this after
    // MapFragment adds support for selectable basemaps.
    public GoogleMap getGoogleMap() {
        return map;
    }

    @Override public @NonNull MapPoint getCenter() {
        if (map == null) {  // during Robolectric tests, map will be null
            return fromLatLng(INITIAL_CENTER);
        }
        LatLng target = map.getCameraPosition().target;
        return new MapPoint(target.latitude, target.longitude);
    }

    @Override public void setCenter(@Nullable MapPoint center, boolean animate) {
        if (map == null) {  // during Robolectric tests, map will be null
            return;
        }
        if (center != null) {
            moveOrAnimateCamera(CameraUpdateFactory.newLatLng(toLatLng(center)), animate);
        }
    }

    @Override public double getZoom() {
        if (map == null) {  // during Robolectric tests, map will be null
            return INITIAL_ZOOM;
        }
        return map.getCameraPosition().zoom;
    }

    @Override public void zoomToPoint(@Nullable MapPoint center, boolean animate) {
        zoomToPoint(center, POINT_ZOOM, animate);
    }

    @Override public void zoomToPoint(@Nullable MapPoint center, double zoom, boolean animate) {
        if (map == null) {  // during Robolectric tests, map will be null
            return;
        }
        if (center != null) {
            moveOrAnimateCamera(
                CameraUpdateFactory.newLatLngZoom(toLatLng(center), (float) zoom), animate);
        }
    }

    @Override public void zoomToBoundingBox(Iterable<MapPoint> points, double scaleFactor, boolean animate) {
        if (map == null) {  // during Robolectric tests, map will be null
            return;
        }
        if (points != null) {
            int count = 0;
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            MapPoint lastPoint = null;
            for (MapPoint point : points) {
                lastPoint = point;
                builder.include(toLatLng(point));
                count++;
            }
            if (count == 1) {
                zoomToPoint(lastPoint, animate);
            } else if (count > 1) {
                final LatLngBounds bounds = expandBounds(builder.build(), 1 / scaleFactor);
                new Handler().postDelayed(() -> {
                    moveOrAnimateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0), animate);
                }, 100);
            }
        }
    }

    protected LatLngBounds expandBounds(LatLngBounds bounds, double factor) {
        double north = bounds.northeast.latitude;
        double south = bounds.southwest.latitude;
        double latCenter = (north + south) / 2;
        double latRadius = ((north - south) / 2) * factor;
        north = Math.min(90, latCenter + latRadius);
        south = Math.max(-90, latCenter - latRadius);

        double east = bounds.northeast.longitude;
        double west = bounds.southwest.longitude;
        while (east < west) {
            east += 360;
        }
        double lonCenter = (east + west) / 2;
        double lonRadius = Math.min(180 - 1e-6, ((east - west) / 2) * factor);
        east = lonCenter + lonRadius;
        west = lonCenter - lonRadius;

        return new LatLngBounds(new LatLng(south, west), new LatLng(north, east));
    }

    protected void moveOrAnimateCamera(CameraUpdate movement, boolean animate) {
        if (animate) {
            map.animateCamera(movement);
        } else {
            map.moveCamera(movement);
        }
    }

    @Override public int addMarker(MapPoint point, boolean draggable) {
        int featureId = nextFeatureId++;
        features.put(featureId, new MarkerFeature(map, point, draggable));
        return featureId;
    }

    @Override public @Nullable MapPoint getMarkerPoint(int featureId) {
        MapFeature feature = features.get(featureId);
        return feature instanceof MarkerFeature ? ((MarkerFeature) feature).getPoint() : null;
    }

    @Override public int addDraggablePoly(@NonNull Iterable<MapPoint> points, boolean closedPolygon) {
        int featureId = nextFeatureId++;
        features.put(featureId, new PolyFeature(map, points, closedPolygon));
        return featureId;
    }

    @Override public void appendPointToPoly(int featureId, @NonNull MapPoint point) {
        MapFeature feature = features.get(featureId);
        if (feature instanceof PolyFeature) {
            ((PolyFeature) feature).addPoint(point);
        }
    }

    @Override public @NonNull List<MapPoint> getPolyPoints(int featureId) {
        MapFeature feature = features.get(featureId);
        if (feature instanceof PolyFeature) {
            return ((PolyFeature) feature).getPoints();
        }
        return new ArrayList<>();
    }

    @Override public void removePolyLastPoint(int featureId) {
        MapFeature feature = features.get(featureId);
        if (feature instanceof PolyFeature) {
            ((PolyFeature) feature).removeLastPoint();
        }
    }

    @Override public void removeFeature(int featureId) {
        MapFeature feature = features.remove(featureId);
        if (feature != null) {
            feature.dispose();
        }
    }

    @Override public void clearFeatures() {
        if (map != null) {  // during Robolectric tests, map will be null
            for (MapFeature feature : features.values()) {
                feature.dispose();
            }
        }
        features.clear();
    }

    @Override public void setClickListener(@Nullable PointListener listener) {
        clickListener = listener;
    }

    @Override public void setLongPressListener(@Nullable PointListener listener) {
        longPressListener = listener;
    }

    @Override public void setDragEndListener(@Nullable FeatureListener listener) {
        dragEndListener = listener;
    }

    @Override public void setGpsLocationListener(@Nullable PointListener listener) {
        gpsLocationListener = listener;
    }

    @Override public void setGpsLocationEnabled(boolean enable) {
        if (enable != gpsLocationEnabled) {
            gpsLocationEnabled = enable;
            if (locationClient == null) {
                locationClient = LocationClients.clientForContext(getActivity());
                locationClient.setListener(this);
            }
            if (enable) {
                locationClient.start();
            } else {
                locationClient.stop();
            }
        }
    }

    @Override public void runOnGpsLocationReady(@NonNull ReadyListener listener) {
        if (lastLocationFix != null) {
            listener.onReady(this);
        } else {
            gpsLocationReadyListeners.add(listener);
        }
    }

    @Override public void onLocationChanged(Location location) {
        lastLocationFix = fromLocation(location);
        lastLocationProvider = location.getProvider();
        for (ReadyListener listener : gpsLocationReadyListeners) {
            listener.onReady(this);
        }
        gpsLocationReadyListeners.clear();
        if (gpsLocationListener != null) {
            gpsLocationListener.onPoint(lastLocationFix);
        }

        if (getActivity() != null) {
            updateLocationIndicator(toLatLng(lastLocationFix), location.getAccuracy());
        }
    }

    protected void updateLocationIndicator(LatLng loc, double radius) {
        if (map == null) {
            return;
        }
        if (locationCrosshairs == null) {
            locationCrosshairs = map.addMarker(new MarkerOptions()
                .position(loc)
                .icon(getBitmapDescriptor(R.drawable.ic_crosshairs))
                .anchor(0.5f, 0.5f)  // center the crosshairs on the position
            );
        }
        if (accuracyCircle == null) {
            int stroke = getResources().getColor(R.color.locationAccuracyCircle);
            int fill = getResources().getColor(R.color.locationAccuracyFill);
            accuracyCircle = map.addCircle(new CircleOptions()
                .center(loc)
                .radius(radius)
                .strokeWidth(1)
                .strokeColor(stroke)
                .fillColor(fill)
            );
        }

        locationCrosshairs.setPosition(loc);
        accuracyCircle.setCenter(loc);
        accuracyCircle.setRadius(radius);
    }

    @Override public @Nullable MapPoint getGpsLocation() {
        return lastLocationFix;
    }

    @Override public @Nullable String getLocationProvider() {
        return lastLocationProvider;
    }

    @Override public void onMapClick(LatLng latLng) {
        if (clickListener != null) {
            clickListener.onPoint(fromLatLng(latLng));
        }
    }

    @Override public void onMapLongClick(LatLng latLng) {
        if (longPressListener != null) {
            longPressListener.onPoint(fromLatLng(latLng));
        }
    }

    @Override public boolean onMarkerClick(Marker marker) {
        onMapClick(marker.getPosition());
        return true;
    }

    @Override public void onMarkerDragStart(Marker marker) {
        // When dragging starts, GoogleMap makes the marker jump up to move it
        // out from under the user's finger; whenever a marker moves, we have
        // to update its corresponding feature.
        updateFeature(findFeature(marker));
    }

    @Override public void onMarkerDrag(Marker marker) {
        // When a marker is manually dragged, the position is no longer
        // obtained from a GPS reading, so the altitude and standard deviation
        // fields are no longer meaningful; reset them to zero.
        marker.setSnippet("0;0");
        updateFeature(findFeature(marker));
    }

    @Override public void onMarkerDragEnd(Marker marker) {
        int featureId = findFeature(marker);
        updateFeature(featureId);
        if (dragEndListener != null && featureId != -1) {
            dragEndListener.onFeature(featureId);
        }
    }

    @Override public void onClientStart() {
        lastLocationFix = fromLocation(locationClient.getLastLocation());
        locationClient.requestLocationUpdates(this);
        if (!locationClient.isLocationAvailable()) {
            showGpsDisabledAlert();
        }
    }

    @Override public void onClientStartFailure() {
        showGpsDisabledAlert();
    }

    @Override public void onClientStop() {
        locationClient.stopLocationUpdates();
    }

    protected void showGpsDisabledAlert() {
        gpsErrorDialog = new AlertDialog.Builder(getActivity())
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

    /** Finds the feature to which the given marker belongs. */
    protected int findFeature(Marker marker) {
        for (int featureId : features.keySet()) {
            if (features.get(featureId).ownsMarker(marker)) {
                return featureId;
            }
        }
        return -1;  // not found
    }

    protected void updateFeature(int featureId) {
        MapFeature feature = features.get(featureId);
        if (feature != null) {
            feature.update();
        }
    }

    protected static @NonNull MapPoint fromLatLng(@NonNull LatLng latLng) {
        return new MapPoint(latLng.latitude, latLng.longitude);
    }

    protected static @Nullable MapPoint fromLocation(@Nullable Location location) {
        if (location == null) {
            return null;
        }
        return new MapPoint(location.getLatitude(), location.getLongitude(),
            location.getAltitude(), location.getAccuracy());
    }

    protected static @NonNull MapPoint fromMarker(@NonNull Marker marker) {
        LatLng position = marker.getPosition();
        String snippet = marker.getSnippet();
        String[] parts = (snippet != null ? snippet : "").split(";");
        double alt = 0;
        double sd = 0;
        try {
            if (parts.length >= 1) {
                alt = Double.parseDouble(parts[0]);
            }
            if (parts.length >= 2) {
                sd = Double.parseDouble(parts[1]);
            }
        } catch (NumberFormatException e) {
            Timber.w("Marker.getSnippet() did not contain two numbers");
        }
        return new MapPoint(position.latitude, position.longitude, alt, sd);
    }

    protected static @NonNull LatLng toLatLng(@NonNull MapPoint point) {
        return new LatLng(point.lat, point.lon);
    }

    protected Marker createMarker(GoogleMap map, MapPoint point, boolean draggable) {
        if (map == null || getActivity() == null) {  // during Robolectric tests, map will be null
            return null;
        }
        // A Marker's position is a LatLng with just latitude and longitude
        // fields.  We need to store the point's altitude and standard
        // deviation values somewhere, so they go in the marker's snippet.
        return map.addMarker(new MarkerOptions()
            .position(toLatLng(point))
            .snippet(point.alt + ";" + point.sd)
            .draggable(draggable)
            .icon(getBitmapDescriptor(R.drawable.ic_map_point))
            .anchor(0.5f, 0.5f)  // center the icon on the position
        );
    }

    protected BitmapDescriptor getBitmapDescriptor(int drawableId) {
        return BitmapDescriptorFactory.fromBitmap(
            IconUtils.getBitmap(getActivity(), drawableId));
    }

    @VisibleForTesting public boolean isGpsErrorDialogShowing() {
        return gpsErrorDialog != null && gpsErrorDialog.isShowing();
    }

    /**
     * A MapFeature is a physical feature on a map, such as a point, a road,
     * a building, a region, etc.  It is presented to the user as one editable
     * object, though its appearance may be constructed from multiple overlays
     * (e.g. geometric elements, handles for manipulation, etc.).
     */
    interface MapFeature {
        /** Returns true if the given marker belongs to this feature. */
        boolean ownsMarker(Marker marker);

        /** Updates the feature's geometry after any UI handles have moved. */
        void update();

        /** Removes the feature from the map, leaving it no longer usable. */
        void dispose();
    }

    protected class MarkerFeature implements MapFeature {
        Marker marker;

        public MarkerFeature(GoogleMap map, MapPoint point, boolean draggable) {
            this.marker = createMarker(map, point, draggable);
        }

        public MapPoint getPoint() {
            return fromMarker(marker);
        }

        public boolean ownsMarker(Marker givenMarker) {
            return marker.equals(givenMarker);
        }

        public void update() { }

        public void dispose() {
            marker.remove();
            marker = null;
        }
    }

    /** A polyline or polygon that can be manipulated by dragging markers at its vertices. */
    protected class PolyFeature implements MapFeature {
        final GoogleMap map;
        final List<Marker> markers = new ArrayList<>();
        final boolean closedPolygon;
        Polyline polyline;
        public static final int STROKE_WIDTH = 5;

        public PolyFeature(GoogleMap map, Iterable<MapPoint> points, boolean closedPolygon) {
            this.map = map;
            this.closedPolygon = closedPolygon;
            if (map == null) {  // during Robolectric tests, map will be null
                return;
            }
            for (MapPoint point : points) {
                markers.add(createMarker(map, point, true));
            }
            update();
        }

        public boolean ownsMarker(Marker givenMarker) {
            return markers.contains(givenMarker);
        }

        public void update() {
            List<LatLng> latLngs = new ArrayList<>();
            for (Marker marker : markers) {
                latLngs.add(marker.getPosition());
            }
            if (closedPolygon && !latLngs.isEmpty()) {
                latLngs.add(latLngs.get(0));
            }
            if (markers.isEmpty()) {
                clearPolyline();
            } else if (polyline == null) {
                polyline = map.addPolyline(new PolylineOptions()
                    .color(getResources().getColor(R.color.mapLine))
                    .zIndex(1)
                    .width(STROKE_WIDTH)
                    .addAll(latLngs)
                );
            } else {
                polyline.setPoints(latLngs);
            }
        }

        public void dispose() {
            clearPolyline();
            for (Marker marker : markers) {
                marker.remove();
            }
            markers.clear();
        }

        public List<MapPoint> getPoints() {
            List<MapPoint> points = new ArrayList<>();
            for (Marker marker : markers) {
                points.add(fromMarker(marker));
            }
            return points;
        }

        public void addPoint(MapPoint point) {
            if (map == null) {  // during Robolectric tests, map will be null
                return;
            }
            markers.add(createMarker(map, point, true));
            update();
        }

        public void removeLastPoint() {
            if (!markers.isEmpty()) {
                int last = markers.size() - 1;
                markers.get(last).remove();
                markers.remove(last);
                update();
            }
        }

        protected void clearPolyline() {
            if (polyline != null) {
                polyline.remove();
                polyline = null;
            }
        }
    }
}
