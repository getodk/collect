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
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.odk.collect.android.R;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class GoogleMapFragment extends SupportMapFragment implements
    MapFragment, LocationListener, LocationClient.LocationClientListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener,
    GoogleMap.OnMarkerDragListener {
    protected GoogleMap map;
    protected List<MapFragment.ReadyListener> gpsLocationReadyListeners = new ArrayList<>();
    protected MapFragment.PointListener clickListener;
    protected MapFragment.PointListener longPressListener;
    protected LocationClient locationClient;
    protected MapPoint lastLocationFix;
    protected int nextFeatureId = 1;
    protected Map<Integer, MapFeature> features = new HashMap<>();
    protected AlertDialog gpsErrorDialog;
    protected boolean gpsLocationEnabled;

    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "This flag is exposed for Robolectric tests to set")
    @VisibleForTesting public static boolean callReadyListenerSynchronously;

    @Override public void addTo(@NonNull FragmentActivity activity, int containerId, @Nullable ReadyListener listener) {
        activity.getSupportFragmentManager()
            .beginTransaction().add(containerId, this).commitNow();
        getMapAsync((GoogleMap map) -> {
            if (map == null) {
                ToastUtils.showShortToast(R.string.google_play_services_error_occured);
                if (listener != null) {
                    listener.onReady(null);
                }
                return;
            }
            this.map = map;
            map.setOnMapClickListener(this);
            map.setOnMapLongClickListener(this);
            map.setOnMarkerDragListener(this);
            map.getUiSettings().setCompassEnabled(true);
            // Show the blue dot on the map, but hide the Google-provided
            // "go to my location" button; we have our own button for that.
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            if (listener != null) {
                listener.onReady(this);
            }
        });

        // In Robolectric tests, getMapAsync() never gets around to calling its
        // callback; we have to invoke the ready listener directly.
        if (callReadyListenerSynchronously) {
            listener.onReady(this);
        }
    }

    // TOOD(ping): This method is only used by MapHelper.  Remove this after
    // MapFragment adds support for selectable basemaps.
    public GoogleMap getGoogleMap() {
        return map;
    }

    @Override public @NonNull MapPoint getCenter() {
        LatLng target = map.getCameraPosition().target;
        return new MapPoint(target.latitude, target.longitude);
    }

    @Override public double getZoom() {
        return map.getCameraPosition().zoom;
    }

    @Override public void zoomToPoint(@NonNull MapPoint center, double zoom) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(toLatLng(center), (float) zoom));
    }

    @Override public void zoomToBoundingBox(Iterable<MapPoint> points, double scaleFactor) {
        if (points != null) {
            int count = 0;
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (MapPoint point : points) {
                builder.include(toLatLng(point));
                count++;
            }
            if (count > 0) {
                final LatLngBounds bounds = expandBounds(builder.build(), 1 / scaleFactor);
                new Handler().postDelayed(() -> {
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
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

    @Override public int addDraggableShape(@NonNull Iterable<MapPoint> points) {
        int featureId = nextFeatureId++;
        features.put(featureId, new DraggableShape(map, points));
        return featureId;
    }

    @Override public void appendPointToShape(int featureId, @NonNull MapPoint point) {
        MapFeature feature = features.get(featureId);
        if (feature instanceof DraggableShape) {
            ((DraggableShape) feature).addPoint(point);
        }
    }

    @Override public @NonNull List<MapPoint> getPointsOfShape(int featureId) {
        MapFeature feature = features.get(featureId);
        if (feature instanceof DraggableShape) {
            return ((DraggableShape) feature).getPoints();
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
        map.clear();
        features.clear();
    }

    @Override public void setClickListener(@Nullable PointListener listener) {
        clickListener = listener;
    }

    @Override public void setLongPressListener(@Nullable PointListener listener) {
        longPressListener = listener;
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
        lastLocationFix = new MapPoint(location.getLatitude(), location.getLongitude());
        for (ReadyListener listener : gpsLocationReadyListeners) {
            listener.onReady(this);
        }
        gpsLocationReadyListeners.clear();
    }

    @Override public @Nullable MapPoint getGpsLocation() {
        return lastLocationFix;
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

    @Override public void onMarkerDragStart(Marker marker) {
        updateFeatures();
    }

    @Override public void onMarkerDrag(Marker marker) {
        updateFeatures();
    }

    @Override public void onMarkerDragEnd(Marker marker) {
        updateFeatures();
    }

    @Override public void onClientStart() {
        locationClient.requestLocationUpdates(this);
        if (!locationClient.isLocationAvailable()) {
            showGpsDisabledAlert();
        }
    }

    @Override public void onClientStartFailure() {
        showGpsDisabledAlert();
    }

    @Override public void onClientStop() { }

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

    @VisibleForTesting public boolean isGpsErrorDialogShowing() {
        return gpsErrorDialog != null && gpsErrorDialog.isShowing();
    }

    protected void updateFeatures() {
        for (MapFeature feature : features.values()) {
            feature.update();
        }
    }

    protected static double clamp(double x, double min, double max) {
        return x < min ? min : x > max ? max : x;
    }

    protected static @NonNull MapPoint fromLatLng(@NonNull LatLng latLng) {
        return new MapPoint(latLng.latitude, latLng.longitude);
    }

    protected static @NonNull LatLng toLatLng(@NonNull MapPoint point) {
        return new LatLng(point.lat, point.lon);
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

    /** A polygon that can be manipulated by dragging markers at its vertices. */
    protected static class DraggableShape implements MapFeature {
        final GoogleMap map;
        final List<Marker> markers = new ArrayList<>();
        Polygon polygon;

        public DraggableShape(GoogleMap map, Iterable<MapPoint> points) {
            this.map = map;
            for (MapPoint point : points) {
                markers.add(map.addMarker(
                    new MarkerOptions().position(toLatLng(point)).draggable(true)));
            }
            // We don't always add a Polygon, because GoogleMap.addPolygon() will
            // crash with zero points; let update() decide whether to create one.
            update();
        }

        public void update() {
            if (markers.isEmpty()) {
                if (polygon != null) {
                    polygon.remove();
                    polygon = null;
                }
                return;
            }

            List<LatLng> latLngs = new ArrayList<>();
            for (Marker marker : markers) {
                latLngs.add(marker.getPosition());
            }
            if (polygon == null) {
                PolygonOptions polyOpts = new PolygonOptions();
                polyOpts.strokeColor(Color.RED);
                polyOpts.zIndex(1);
                polyOpts.addAll(latLngs);
                polygon = map.addPolygon(polyOpts);
            } else {
                polygon.setPoints(latLngs);
            }
        }

        public void dispose() {
            for (Marker marker : markers) {
                marker.remove();
            }
            markers.clear();
            polygon.remove();
            polygon = null;
        }

        public List<MapPoint> getPoints() {
            List<MapPoint> points = new ArrayList<>();
            for (Marker marker : markers) {
                points.add(fromLatLng(marker.getPosition()));
            }
            return points;
        }

        public void addPoint(MapPoint point) {
            markers.add(map.addMarker(
                new MarkerOptions().position(toLatLng(point)).draggable(true)));
            update();
        }
    }
}
