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

package org.odk.collect.android.geo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.LocationListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.utilities.IconUtils;
import org.odk.collect.android.utilities.MapFragmentReferenceLayerUtils;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.geo.maps.MapFragment;
import org.odk.collect.geo.maps.MapPoint;
import org.odk.collect.location.LocationClient;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

/** A MapFragment drawn by OSMDroid. */
public class OsmDroidMapFragment extends Fragment implements MapFragment,
    MapEventsReceiver, IRegisterReceiver,
    LocationListener, LocationClient.LocationClientListener {

    // Bundle keys understood by applyConfig().
    static final String KEY_WEB_MAP_SERVICE = "WEB_MAP_SERVICE";

    @Inject
    MapProvider mapProvider;

    @Inject
    ReferenceLayerRepository referenceLayerRepository;

    @Inject
    LocationClient locationClient;

    private MapView map;
    private ReadyListener readyListener;
    private PointListener clickListener;
    private PointListener longPressListener;
    private PointListener gpsLocationListener;
    private FeatureListener featureClickListener;
    private FeatureListener dragEndListener;
    private MyLocationNewOverlay myLocationOverlay;
    private OsmLocationClientWrapper osmLocationClientWrapper;
    private int nextFeatureId = 1;
    private final Map<Integer, MapFeature> features = new HashMap<>();
    private boolean clientWantsLocationUpdates;
    private IGeoPoint lastMapCenter;
    private WebMapService webMapService;
    private File referenceLayerFile;
    private TilesOverlay referenceOverlay;

    @Override public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        Context context = getActivity();
        return context != null ? context.registerReceiver(receiver, filter) : null;
    }

    @Override public void unregisterReceiver(BroadcastReceiver receiver) {
        Context context = getActivity();
        if (context != null) {
            context.unregisterReceiver(receiver);
        }
    }

    @Override public void destroy() { }

    @Override public void addTo(
        @NonNull FragmentActivity activity, int containerId,
        @Nullable ReadyListener readyListener, @Nullable ErrorListener errorListener) {
        this.readyListener = readyListener;
        // If the containing activity is being re-created upon screen rotation,
        // the FragmentManager will have also re-created a copy of the previous
        // OsmDroidMapFragment.  We don't want these useless copies of old fragments
        // to linger, so the following line calls .replace() instead of .add().
        activity.getSupportFragmentManager()
            .beginTransaction().replace(containerId, this).commit();
    }

    @Override public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override public void onStart() {
        super.onStart();
        mapProvider.onMapFragmentStart(this);
    }

    @Override public void onResume() {
        super.onResume();
        enableLocationUpdates(clientWantsLocationUpdates);
    }

    @Override public void onPause() {
        super.onPause();
        enableLocationUpdates(false);
    }

    @Override public void onStop() {
        mapProvider.onMapFragmentStop(this);
        super.onStop();
    }

    @Override public void onDestroy() {
        clearFeatures();  // prevent a memory leak due to refs held by markers
        super.onDestroy();
    }

    @Override public void applyConfig(Bundle config) {
        webMapService = (WebMapService) config.getSerializable(KEY_WEB_MAP_SERVICE);
        referenceLayerFile = MapFragmentReferenceLayerUtils.getReferenceLayerFile(config, referenceLayerRepository);
        if (map != null) {
            map.setTileSource(webMapService.asOnlineTileSource());
            loadReferenceOverlay();
        }
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater,
        @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.osm_map_layout, container, false);
        map = view.findViewById(R.id.osm_map_view);
        if (webMapService != null) {
            map.setTileSource(webMapService.asOnlineTileSource());
        }
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(true);
        map.setMinZoomLevel(2.0);
        map.setMaxZoomLevel(22.0);
        map.getController().setCenter(toGeoPoint(INITIAL_CENTER));
        map.getController().setZoom((int) INITIAL_ZOOM);
        map.setTilesScaledToDpi(true);
        map.setFlingEnabled(false);
        addAttributionAndMapEventsOverlays();
        loadReferenceOverlay();
        addMapLayoutChangeListener(map);

        locationClient.setListener(this);

        osmLocationClientWrapper = new OsmLocationClientWrapper(locationClient);
        myLocationOverlay = new MyLocationNewOverlay(osmLocationClientWrapper, map);
        myLocationOverlay.setDrawAccuracyEnabled(true);
        Bitmap crosshairs = IconUtils.getBitmap(getActivity(), R.drawable.ic_crosshairs);
        myLocationOverlay.setDirectionArrow(crosshairs, crosshairs);
        myLocationOverlay.setPersonHotspot(crosshairs.getWidth() / 2.0f, crosshairs.getHeight() / 2.0f);

        new Handler().postDelayed(() -> {
            // If the screen is rotated before the map is ready, this fragment
            // could already be detached, which makes it unsafe to use.  Only
            // call the ReadyListener if this fragment is still attached.
            if (readyListener != null && getActivity() != null) {
                readyListener.onReady(this);
            }
        }, 100);
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

    @Override public @NonNull
    MapPoint getCenter() {
        return fromGeoPoint(map.getMapCenter());
    }

    @Override public void setCenter(@Nullable MapPoint center, boolean animate) {
        if (center != null) {
            if (animate) {
                map.getController().animateTo(toGeoPoint(center));
            } else {
                map.getController().setCenter(toGeoPoint(center));
            }
        }
    }

    @Override public double getZoom() {
        return map.getZoomLevel();
    }

    @Override public void zoomToPoint(@Nullable MapPoint center, boolean animate) {
        zoomToPoint(center, POINT_ZOOM, animate);
    }

    @Override public void zoomToPoint(@Nullable MapPoint center, double zoom, boolean animate) {
        // We're ignoring the 'animate' flag because OSMDroid doesn't provide
        // support for simultaneously animating the viewport center and zoom level.
        if (center != null) {
            // setCenter() must be done last; setZoom() does not preserve the center.
            map.getController().setZoom((int) Math.round(zoom));
            map.getController().setCenter(toGeoPoint(center));
        }
    }

    @Override public void zoomToBoundingBox(Iterable<MapPoint> points, double scaleFactor, boolean animate) {
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
                zoomToPoint(lastPoint, animate);
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
                new Handler().postDelayed(() -> map.zoomToBoundingBox(box, animate), 100);
            }
        }
    }

    @Override public int addMarker(MapPoint point, boolean draggable, @IconAnchor String iconAnchor) {
        int featureId = nextFeatureId++;
        features.put(featureId, new MarkerFeature(map, point, draggable, iconAnchor));
        return featureId;
    }

    @Override public void setMarkerIcon(int featureId, int drawableId) {
        MapFeature feature = features.get(featureId);
        if (feature instanceof MarkerFeature) {
            ((MarkerFeature) feature).setIcon(drawableId);
        }
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
        map.getOverlays().clear();
        addAttributionAndMapEventsOverlays();
        map.getOverlays().add(myLocationOverlay);
        map.invalidate();
        features.clear();
        nextFeatureId = 1;
    }

    @Override public void setClickListener(@Nullable PointListener listener) {
        clickListener = listener;
    }

    @Override public void setLongPressListener(@Nullable PointListener listener) {
        longPressListener = listener;
    }

    @Override public void setFeatureClickListener(@Nullable FeatureListener listener) {
        featureClickListener = listener;
    }

    @Override public void setDragEndListener(@Nullable FeatureListener listener) {
        dragEndListener = listener;
    }

    @Override public void setGpsLocationListener(@Nullable PointListener listener) {
        gpsLocationListener = listener;
    }

    @Override
    public void setRetainMockAccuracy(boolean retainMockAccuracy) {
        locationClient.setRetainMockAccuracy(retainMockAccuracy);
    }

    @Override public void runOnGpsLocationReady(@NonNull ReadyListener listener) {
        myLocationOverlay.runOnFirstFix(() -> getActivity().runOnUiThread(() -> listener.onReady(this)));
    }

    @Override public void setGpsLocationEnabled(boolean enable) {
        if (enable != clientWantsLocationUpdates) {
            clientWantsLocationUpdates = enable;
            enableLocationUpdates(clientWantsLocationUpdates);
        }
    }

    @Override public @Nullable MapPoint getGpsLocation() {
        return fromLocation(myLocationOverlay);
    }

    @Override public @Nullable String getLocationProvider() {
        Location fix = myLocationOverlay.getLastFix();
        return fix != null ? fix.getProvider() : null;
    }

    @Override public void onLocationChanged(Location location) {
        Timber.i("onLocationChanged: location = %s", location);
        if (gpsLocationListener != null) {
            MapPoint point = fromLocation(myLocationOverlay);
            if (point != null) {
                gpsLocationListener.onPoint(point);
            }
        }

        if (myLocationOverlay != null) {
            myLocationOverlay.onLocationChanged(location, osmLocationClientWrapper);
        }
    }

    @Override public void onClientStart() {
        map.getOverlays().add(myLocationOverlay);
        myLocationOverlay.setEnabled(true);
        myLocationOverlay.enableMyLocation();

        Timber.i("Requesting location updates (to %s)", this);
        locationClient.requestLocationUpdates(this);
    }

    @Override public void onClientStartFailure() {
        showGpsDisabledAlert();
    }

    @Override public void onClientStop() { }

    private void enableLocationUpdates(boolean enable) {
        locationClient.setListener(this);

        if (enable) {
            Timber.i("Starting LocationClient %s (for MapFragment %s)", locationClient, this);
            locationClient.start();
        } else {
            Timber.i("Stopping LocationClient %s (for MapFragment %s)", locationClient, this);
            locationClient.stop();
            myLocationOverlay.setEnabled(false);
            myLocationOverlay.disableFollowLocation();
            myLocationOverlay.disableMyLocation();
        }
    }

    private static @Nullable MapPoint fromLocation(@NonNull MyLocationNewOverlay overlay) {
        GeoPoint geoPoint = overlay.getMyLocation();
        if (geoPoint == null) {
            return null;
        }
        return new MapPoint(
            geoPoint.getLatitude(), geoPoint.getLongitude(),
            geoPoint.getAltitude(), overlay.getLastFix().getAccuracy()
        );
    }

    private static @NonNull MapPoint fromGeoPoint(@NonNull IGeoPoint geoPoint) {
        return new MapPoint(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    private static @NonNull MapPoint fromGeoPoint(@NonNull GeoPoint geoPoint) {
        return new MapPoint(geoPoint.getLatitude(), geoPoint.getLongitude(), geoPoint.getAltitude());
    }

    private static @NonNull MapPoint fromMarker(@NonNull Marker marker) {
        GeoPoint geoPoint = marker.getPosition();
        double sd = 0;
        try {
            sd = Double.parseDouble(marker.getSubDescription());
        } catch (NumberFormatException e) {
            Timber.w("Marker.getSubDescription() did not contain a number");
        }
        return new MapPoint(
            geoPoint.getLatitude(), geoPoint.getLongitude(), geoPoint.getAltitude(), sd
        );
    }

    private static @NonNull GeoPoint toGeoPoint(@NonNull MapPoint point) {
        return new GeoPoint(point.lat, point.lon, point.alt);
    }

    /** Updates the map to reflect the value of referenceLayerFile. */
    private void loadReferenceOverlay() {
        if (referenceOverlay != null) {
            map.getOverlays().remove(referenceOverlay);
            referenceOverlay = null;
        }
        if (referenceLayerFile != null) {
            OsmMBTileProvider mbprovider = new OsmMBTileProvider(this, referenceLayerFile);
            referenceOverlay = new TilesOverlay(mbprovider, getContext());
            referenceOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
            map.getOverlays().add(0, referenceOverlay);
        }
        map.invalidate();
    }

    private void showGpsDisabledAlert() {
        new MaterialAlertDialogBuilder(getContext())
            .setMessage(getString(R.string.gps_enable_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.enable_gps),
                (dialog, id) -> startActivityForResult(
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0))
            .setNegativeButton(getString(R.string.cancel),
                (dialog, id) -> dialog.cancel())
            .create()
            .show();
    }

    /**
     * Adds a listener that keeps track of the map center, and another
     * listener that restores the map center when the MapView's layout changes.
     * We have to do this because the MapView is buggy and fails to preserve its
     * view on a layout change, causing the map viewport to jump around when the
     * screen is resized or rotated in a way that doesn't restart the activity.
     */
    private void addMapLayoutChangeListener(MapView map) {
        lastMapCenter = map.getMapCenter();
        map.setMapListener(new MapListener() {
            @Override public boolean onScroll(ScrollEvent event) {
                lastMapCenter = map.getMapCenter();
                return false;
            }

            @Override public boolean onZoom(ZoomEvent event) {
                lastMapCenter = map.getMapCenter();
                return false;
            }
        });
        map.addOnLayoutChangeListener(
            (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                map.getController().setCenter(lastMapCenter));
    }

    private Marker createMarker(MapView map, MapPoint point, MapFeature feature, @IconAnchor String iconAnchor) {
        // A Marker's position is a GeoPoint with latitude, longitude, and
        // altitude fields.  We need to store the standard deviation value
        // somewhere, so it goes in the marker's sub-description field.
        Marker marker = new Marker(map);
        marker.setPosition(toGeoPoint(point));
        marker.setSubDescription(Double.toString(point.sd));
        marker.setDraggable(feature != null);
        marker.setIcon(ContextCompat.getDrawable(map.getContext(), R.drawable.ic_map_point));
        marker.setAnchor(getIconAnchorValueX(iconAnchor), getIconAnchorValueY(iconAnchor));
        marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
            int featureId = findFeature(clickedMarker);
            if (featureClickListener != null && featureId != -1) {
                featureClickListener.onFeature(featureId);
                return true;  // consume the event
            }
            return false;
        });
        marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override public void onMarkerDragStart(Marker marker) { }

            @Override public void onMarkerDrag(Marker marker) {
                // When a marker is manually dragged, the position is no longer
                // obtained from a GPS reading, so the standard deviation field
                // is no longer meaningful; reset it to zero.
                marker.setSubDescription("0");
                updateFeature(findFeature(marker));
            }

            @Override public void onMarkerDragEnd(Marker marker) {
                int featureId = findFeature(marker);
                updateFeature(featureId);
                if (dragEndListener != null && featureId != -1) {
                    dragEndListener.onFeature(featureId);
                }
            }
        });

        map.getOverlays().add(marker);
        return marker;
    }

    private float getIconAnchorValueX(@IconAnchor String iconAnchor) {
        switch (iconAnchor) {
            case BOTTOM:
            default:
                return Marker.ANCHOR_CENTER;
        }
    }

    private float getIconAnchorValueY(@IconAnchor String iconAnchor) {
        switch (iconAnchor) {
            case BOTTOM:
                return Marker.ANCHOR_BOTTOM;
            default:
                return Marker.ANCHOR_CENTER;
        }
    }

    /** Finds the feature to which the given marker belongs. */
    private int findFeature(Marker marker) {
        for (int featureId : features.keySet()) {
            if (features.get(featureId).ownsMarker(marker)) {
                return featureId;
            }
        }
        return -1;  // not found
    }

    /** Finds the feature to which the given polyline belongs. */
    private int findFeature(Polyline polyline) {
        for (int featureId : features.keySet()) {
            if (features.get(featureId).ownsPolyline(polyline)) {
                return featureId;
            }
        }
        return -1;  // not found
    }

    private void updateFeature(int featureId) {
        MapFeature feature = features.get(featureId);
        if (feature != null) {
            feature.update();
        }
    }

    private void addAttributionAndMapEventsOverlays() {
        map.getOverlays().add(new AttributionOverlay(getContext()));
        map.getOverlays().add(new MapEventsOverlay(this));
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

        /** Returns true if the given polyline belongs to this feature. */
        boolean ownsPolyline(Polyline polyline);

        /** Updates the feature's geometry after any UI handles have moved. */
        void update();

        /** Removes the feature from the map, leaving it no longer usable. */
        void dispose();
    }

    /** A marker that can optionally be dragged by the user. */
    private class MarkerFeature implements MapFeature {
        final MapView map;
        Marker marker;

        MarkerFeature(MapView map, MapPoint point, boolean draggable, @IconAnchor String iconAnchor) {
            this.map = map;
            this.marker = createMarker(map, point, draggable ? this : null, iconAnchor);
        }

        public void setIcon(int drawableId) {
            marker.setIcon(ContextCompat.getDrawable(map.getContext(), drawableId));
        }

        public MapPoint getPoint() {
            return fromMarker(marker);
        }

        public boolean ownsMarker(Marker givenMarker) {
            return marker.equals(givenMarker);
        }

        public boolean ownsPolyline(Polyline polyline) {
            return false;
        }

        public void update() { }

        public void dispose() {
            map.getOverlays().remove(marker);
            marker = null;
        }
    }

    /** A polyline or polygon that can be manipulated by dragging markers at its vertices. */
    private class PolyFeature implements MapFeature {
        final MapView map;
        final List<Marker> markers = new ArrayList<>();
        final Polyline polyline;
        final boolean closedPolygon;
        static final int STROKE_WIDTH = 5;

        PolyFeature(MapView map, Iterable<MapPoint> points, boolean closedPolygon) {
            this.map = map;
            this.closedPolygon = closedPolygon;
            polyline = new Polyline();
            polyline.setColor(map.getContext().getResources().getColor(R.color.mapLineColor));
            polyline.setOnClickListener((clickedPolyline, mapView, eventPos) -> {
                int featureId = findFeature(clickedPolyline);
                if (featureClickListener != null && featureId != -1) {
                    featureClickListener.onFeature(featureId);
                    return true;  // consume the event
                }
                return false;
            });
            Paint paint = polyline.getPaint();
            paint.setStrokeWidth(STROKE_WIDTH);
            map.getOverlays().add(polyline);
            for (MapPoint point : points) {
                markers.add(createMarker(map, point, this, CENTER));
            }
            update();
        }

        public boolean ownsMarker(Marker givenMarker) {
            return markers.contains(givenMarker);
        }

        public boolean ownsPolyline(Polyline givenPolyline) {
            return polyline.equals(givenPolyline);
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
                points.add(fromMarker(marker));
            }
            return points;
        }

        public void addPoint(MapPoint point) {
            markers.add(createMarker(map, point, this, CENTER));
            update();
        }

        public void removeLastPoint() {
            if (!markers.isEmpty()) {
                int last = markers.size() - 1;
                map.getOverlays().remove(markers.get(last));
                markers.remove(last);
                update();
            }
        }
    }

    /** An overlay that draws an attribution message in the lower-right corner. */
    private static class AttributionOverlay extends Overlay {
        public static final int FONT_SIZE_DP = 12;
        public static final int MARGIN_DP = 10;

        private final Paint paint;

        AttributionOverlay(Context context) {
            super();

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(new ThemeUtils(context).getColorOnSurface());
            paint.setTextSize(FONT_SIZE_DP *
                context.getResources().getDisplayMetrics().density);
            paint.setTextAlign(Paint.Align.RIGHT);
        }

        @Override public void draw(Canvas canvas, MapView map, boolean shadow) {
            String attribution = map.getTileProvider().getTileSource().getCopyrightNotice();
            if (!shadow && !map.isAnimating() && attribution != null && !attribution.isEmpty()) {
                String[] lines = attribution.split("\n");
                float lineHeight = paint.getFontSpacing();
                float x = canvas.getWidth() - MARGIN_DP;
                float y = canvas.getHeight() - MARGIN_DP - lineHeight * lines.length;

                canvas.save();
                canvas.concat(map.getProjection().getInvertedScaleRotateCanvasMatrix());
                for (String line : lines) {
                    y += lineHeight;
                    canvas.drawText(line, x, y, paint);
                }
                canvas.restore();
            }
        }
    }

    private static class OsmLocationClientWrapper implements IMyLocationProvider {
        private LocationClient locationClient;

        OsmLocationClientWrapper(LocationClient locationClient) {
            this.locationClient = locationClient;
        }

        @Override
        public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
            // locationClient.start launches async work and we need to be confident that
            // getLastKnownLocation is never called before onClientStart so we don't let the OSM
            // location overlay start the provider. We also ignore the location consumer passed in
            // and instead explicitly forward location updates to the overlay from onLocationChanged
            return true;
        }

        @Override
        public void stopLocationProvider() {
            locationClient.stop();
        }

        @Override
        public Location getLastKnownLocation() {
            return locationClient.getLastLocation();
        }

        @Override
        public void destroy() {
            locationClient.stop();
            locationClient = null;
        }
    }
}
