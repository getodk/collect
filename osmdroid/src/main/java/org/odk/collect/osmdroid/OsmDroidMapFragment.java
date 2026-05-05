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

package org.odk.collect.osmdroid;

import static androidx.core.graphics.drawable.BitmapDrawableKt.toDrawable;
import static org.odk.collect.maps.markers.MarkerIconCreator.toBitmap;
import static org.odk.collect.maps.traces.TraceDescriptionKt.getMarkersForPoints;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.androidshared.system.ContextExt;
import org.odk.collect.maps.MapConfigurator;
import org.odk.collect.maps.MapFragment;
import org.odk.collect.maps.MapPoint;
import org.odk.collect.maps.MapViewModel;
import org.odk.collect.maps.MapViewModelMapFragment;
import org.odk.collect.maps.Zoom;
import org.odk.collect.maps.ZoomObserver;
import org.odk.collect.maps.circles.CircleDescription;
import org.odk.collect.maps.layers.MapFragmentReferenceLayerUtils;
import org.odk.collect.maps.layers.ReferenceLayerRepository;
import org.odk.collect.maps.markers.MarkerDescription;
import org.odk.collect.maps.markers.MarkerIconCreator;
import org.odk.collect.maps.markers.MarkerIconDescription;
import org.odk.collect.maps.traces.LineDescription;
import org.odk.collect.maps.traces.PolygonDescription;
import org.odk.collect.settings.SettingsProvider;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * A MapFragment drawn by OSMDroid.
 */
public class OsmDroidMapFragment extends MapViewModelMapFragment {

    // Bundle keys understood by applyConfig().
    public static final String KEY_WEB_MAP_SERVICE = "WEB_MAP_SERVICE";

    @Inject
    ReferenceLayerRepository referenceLayerRepository;

    @Inject
    MapConfigurator mapConfigurator;

    @Inject
    SettingsProvider settingsProvider;

    private MapView map;
    private ReadyListener readyListener;
    private PointListener clickListener;
    private PointListener longPressListener;
    private FeatureListener featureClickListener;
    private FeatureListener dragEndListener;
    private int nextFeatureId = 1;
    private final Map<Integer, MapFeature> features = new HashMap<>();
    private IGeoPoint lastMapCenter;
    private WebMapService webMapService;
    private File referenceLayerFile;
    private TilesOverlay referenceOverlay;
    private boolean isSystemZooming;
    private MapViewModel mapViewModel;

    @Override
    public void init(@Nullable ReadyListener readyListener, @Nullable ErrorListener errorListener) {
        this.readyListener = readyListener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        OsmDroidDependencyComponent component = ((OsmDroidDependencyComponentProvider) context.getApplicationContext()).getOsmDroidDependencyComponent();
        component.inject(this);

        ViewModelProvider.Factory viewModelFactory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new MapViewModel(
                        settingsProvider.getUnprotectedSettings(),
                        settingsProvider.getMetaSettings()
                );
            }
        };

        mapViewModel = new ViewModelProvider(this, viewModelFactory).get(MapViewModel.class);
    }

    @Override
    public void onDestroy() {
        clearFeatures();  // prevent a memory leak due to refs held by markers
        MarkerIconCreator.clearCache();
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.osm_map_layout, container, false);
        map = view.findViewById(R.id.osm_map_view);
        if (webMapService != null) {
            map.setTileSource(webMapService.asOnlineTileSource());
        }
        map.setMultiTouchControls(true);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMinZoomLevel(2.0);
        map.getController().setCenter(toGeoPoint(MapFragment.Companion.getINITIAL_CENTER()));
        map.getController().setZoom((int) INITIAL_ZOOM);
        map.setTilesScaledToDpi(true);
        map.setFlingEnabled(false);
        map.getOverlays().add(new ScaleBarOverlay(map));
        map.addMapListener(new MapListener() {

            private boolean initialized;

            @Override
            public boolean onZoom(ZoomEvent event) {
                if (!isSystemZooming) {
                    mapViewModel.onUserZoom(getCenter(), event.getZoomLevel());
                }
                return false;
            }

            @Override
            public boolean onScroll(ScrollEvent event) {
                // Ignore initial scroll event that we get when the map loads
                if (initialized) {
                    mapViewModel.onUserMove(getCenter(), event.getSource().getZoomLevelDouble());
                } else {
                    initialized = true;
                }

                return false;
            }
        });
        addAttributionAndMapEventsOverlays();
        loadReferenceOverlay();
        addMapLayoutChangeListener(map);

        new Handler().postDelayed(() -> {
            // If the screen is rotated before the map is ready, this fragment
            // could already be detached, which makes it unsafe to use.  Only
            // call the ReadyListener if this fragment is still attached.
            if (readyListener != null && getActivity() != null) {
                readyListener.onReady(this);
            }
        }, 100);

        mapViewModel.getSettings(mapConfigurator.getPrefKeys()).observe(getViewLifecycleOwner(), settings -> {
            Bundle newConfig = mapConfigurator.buildConfig(settings);
            onConfigChanged(newConfig);
        });

        mapViewModel.getZoom().observe(getViewLifecycleOwner(), new ZoomObserver() {
            @Override
            public void onZoomToPoint(@NonNull Zoom.Point zoom) {
                isSystemZooming = true;
                map.getController().setZoom((int) Math.round(zoom.getLevel()));
                map.getController().setCenter(toGeoPoint(zoom.getPoint()));
                isSystemZooming = false;
            }

            @Override
            public void onZoomToBox(@NonNull Zoom.Box zoom) {
                List<MapPoint> points = zoom.getBox();
                boolean animate = zoom.getAnimate();
                Double scaleFactor = zoom.getScaleFactor();

                int count = 0;
                List<GeoPoint> geoPoints = new ArrayList<>();
                MapPoint lastPoint = null;
                for (MapPoint point : points) {
                    lastPoint = point;
                    geoPoints.add(toGeoPoint(point));
                    count++;
                }
                if (count == 1) {
                    zoomToPoint(lastPoint, zoom.getAnimate());
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
                    new Handler().postDelayed(() -> {
                        isSystemZooming = true;
                        map.zoomToBoundingBox(box, animate);
                        isSystemZooming = false;
                    }, 100);
                }
            }
        });

        return view;
    }

    @Override
    public @NonNull
    MapPoint getCenter() {
        return fromGeoPoint(map.getMapCenter());
    }

    @Override
    public double getZoom() {
        return map.getZoomLevel();
    }

    @Override
    public List<Integer> addMarkers(List<MarkerDescription> markers) {
        List<Integer> featureIds = new ArrayList<>();
        for (MarkerDescription markerDescription : markers) {
            int featureId = nextFeatureId++;
            features.put(featureId, new MarkerFeature(map, markerDescription));
            featureIds.add(featureId);
        }

        map.invalidate();
        return featureIds;
    }

    @Override
    public void setMarkerIcon(int featureId, MarkerIconDescription markerIconDescription) {
        MapFeature feature = features.get(featureId);
        if (feature instanceof MarkerFeature) {
            ((MarkerFeature) feature).setIcon(markerIconDescription);
            map.invalidate();
        }
    }

    @Override
    public @Nullable
    MapPoint getMarkerPoint(int featureId) {
        MapFeature feature = features.get(featureId);
        return feature instanceof MarkerFeature ? ((MarkerFeature) feature).getPoint() : null;
    }

    @Override
    public int addPolyLine(LineDescription lineDescription) {
        int featureId = nextFeatureId++;
        addPolyLine(featureId, lineDescription);
        return featureId;
    }

    private void addPolyLine(int featureId, LineDescription lineDescription) {
        if (lineDescription.getDraggable()) {
            features.put(featureId, new DynamicPolyLineFeature(map, lineDescription));
        } else {
            features.put(featureId, new StaticPolyLineFeature(map, lineDescription));
        }
    }

    @Override
    public void updatePolyLine(int featureId, @NotNull LineDescription lineDescription) {
        features.get(featureId).dispose();
        addPolyLine(featureId, lineDescription);
    }

    @Override
    public int addPolygon(PolygonDescription polygonDescription) {
        int featureId = nextFeatureId++;
        addPolygon(featureId, polygonDescription);
        return featureId;
    }

    private void addPolygon(int featureId, PolygonDescription polygonDescription) {
        if (polygonDescription.getDraggable()) {
            features.put(featureId, new DynamicPolygonFeature(map, polygonDescription));
        } else {
            features.put(featureId, new StaticPolygonFeature(map, polygonDescription));
        }
    }

    @Override
    public void updatePolygon(int featureId, @NotNull PolygonDescription polygonDescription) {
        features.get(featureId).dispose();
        addPolygon(featureId, polygonDescription);
    }

    @Override
    public @NonNull
    List<MapPoint> getPolyPoints(int featureId) {
        MapFeature feature = features.get(featureId);
        if (feature instanceof LineFeature) {
            return ((LineFeature) feature).getPoints();
        }
        return new ArrayList<>();
    }

    @Override
    public void clearFeatures() {
        for (MapFeature feature : features.values()) {
            feature.dispose();
        }
        features.clear();

        if (map != null) {
            map.invalidate();
        }

        nextFeatureId = 1;
    }
    @Override
    public void clearFeatures(@NotNull List<@NotNull Integer> ids) {
        for (Integer id : ids) {
            features.remove(id).dispose();
        }

        map.invalidate();
    }

    @Override
    public void setClickListener(@Nullable PointListener listener) {
        clickListener = listener;
    }

    @Override
    public void setLongPressListener(@Nullable PointListener listener) {
        longPressListener = listener;
    }

    @Override
    public void setFeatureClickListener(@Nullable FeatureListener listener) {
        featureClickListener = listener;
    }

    @Override
    public void setDragEndListener(@Nullable FeatureListener listener) {
        dragEndListener = listener;
    }



    private static @NonNull
    MapPoint fromGeoPoint(@NonNull IGeoPoint geoPoint) {
        return new MapPoint(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    private static @NonNull
    MapPoint fromGeoPoint(@NonNull GeoPoint geoPoint) {
        return new MapPoint(geoPoint.getLatitude(), geoPoint.getLongitude(), geoPoint.getAltitude());
    }

    private static @NonNull
    MapPoint fromMarker(@NonNull Marker marker) {
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

    private static @NonNull
    GeoPoint toGeoPoint(@NonNull MapPoint point) {
        return new GeoPoint(point.latitude, point.longitude, point.altitude);
    }

    /**
     * Updates the map to reflect the value of referenceLayerFile.
     */
    private void loadReferenceOverlay() {
        if (referenceOverlay != null) {
            map.getOverlays().remove(referenceOverlay);
            referenceOverlay.onDetach(map);
            referenceOverlay = null;
        }
        if (referenceLayerFile != null) {
            OsmMBTileProvider mbprovider = new OsmMBTileProvider(new RegisterReceiver(requireActivity()), referenceLayerFile);
            referenceOverlay = new TilesOverlay(mbprovider, getContext());
            referenceOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
            map.getOverlays().add(0, referenceOverlay);
        }
        map.invalidate();
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
            @Override
            public boolean onScroll(ScrollEvent event) {
                lastMapCenter = map.getMapCenter();
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                lastMapCenter = map.getMapCenter();
                return false;
            }
        });
        map.addOnLayoutChangeListener(
                (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                        map.getController().setCenter(lastMapCenter));
    }

    private Marker createMarker(MapView map, MarkerDescription markerDescription) {
        // A Marker's position is a GeoPoint with latitude, longitude, and
        // altitude fields.  We need to store the standard deviation value
        // somewhere, so it goes in the marker's sub-description field.
        Marker marker = new Marker(map);
        marker.setPosition(toGeoPoint(markerDescription.getPoint()));
        marker.setSubDescription(Double.toString(markerDescription.getPoint().accuracy));
        marker.setDraggable(markerDescription.isDraggable());
        Bitmap iconBitmap = toBitmap(markerDescription.getIconDescription(), requireContext());
        marker.setIcon(toDrawable(iconBitmap, requireContext().getResources()));
        marker.setAnchor(getIconAnchorValueX(markerDescription.getIconAnchor()), getIconAnchorValueY(markerDescription.getIconAnchor()));
        marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
            int featureId = findFeature(clickedMarker);
            if (featureClickListener != null && featureId != -1) {
                featureClickListener.onFeature(featureId);
                return true;  // consume the event
            }
            return false;
        });
        marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // When a marker is manually dragged, the position is no longer
                // obtained from a GPS reading, so the standard deviation field
                // is no longer meaningful; reset it to zero.
                marker.setSubDescription("0");
                updateFeature(findFeature(marker));
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
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

    private float getIconAnchorValueX(MapFragment.IconAnchor iconAnchor) {
        switch (iconAnchor) {
            case BOTTOM:
            default:
                return Marker.ANCHOR_CENTER;
        }
    }

    private float getIconAnchorValueY(MapFragment.IconAnchor iconAnchor) {
        switch (iconAnchor) {
            case BOTTOM:
                return Marker.ANCHOR_BOTTOM;
            default:
                return Marker.ANCHOR_CENTER;
        }
    }

    /**
     * Finds the feature to which the given marker belongs.
     */
    private int findFeature(Marker marker) {
        for (int featureId : features.keySet()) {
            if (features.get(featureId).ownsMarker(marker)) {
                return featureId;
            }
        }
        return -1;  // not found
    }

    /**
     * Finds the feature to which the given polyline belongs.
     */
    private int findFeature(Polyline polyline) {
        for (int featureId : features.keySet()) {
            if (features.get(featureId).ownsPolyline(polyline)) {
                return featureId;
            }
        }
        return -1;  // not found
    }

    private int findFeature(Polygon polygon) {
        for (int featureId : features.keySet()) {
            if (features.get(featureId).ownsPolygon(polygon)) {
                return featureId;
            }
        }
        return -1;
    }

    private void updateFeature(int featureId) {
        MapFeature feature = features.get(featureId);
        if (feature != null) {
            feature.update();
        }
    }

    private void addAttributionAndMapEventsOverlays() {
        map.getOverlays().add(new AttributionOverlay(getContext()));
        map.getOverlays().add(
                new MapEventsOverlay(
                        new MapEventsReceiver(
                                point -> {
                                    if (clickListener != null) {
                                        clickListener.onPoint(point);
                                    }
                                },
                                point -> {
                                    if (longPressListener != null) {
                                        longPressListener.onPoint(point);
                                    }
                                }
                        )
                )
        );
    }

    private void onConfigChanged(Bundle config) {
        webMapService = (WebMapService) config.getSerializable(KEY_WEB_MAP_SERVICE);
        referenceLayerFile = MapFragmentReferenceLayerUtils.getReferenceLayerFile(config, referenceLayerRepository);
        if (map != null) {
            map.setTileSource(webMapService.asOnlineTileSource());
            loadReferenceOverlay();
        }
    }

    @NonNull
    @Override
    public MapViewModel getMapViewModel() {
        return mapViewModel;
    }

    @Override
    public void updateMarker(int featureId, @NotNull MarkerDescription markerDescription) {
        features.get(featureId).dispose();
        features.put(featureId, new MarkerFeature(map, markerDescription));
        map.invalidate();
    }

    @Override
    public int addCircle(@NotNull CircleDescription circleDescription) {
        return -1;
    }

    @Override
    public void updateCircle(int featureId, @NotNull CircleDescription circleDescription) {

    }

    /**
     * A MapFeature is a physical feature on a map, such as a point, a road,
     * a building, a region, etc.  It is presented to the user as one editable
     * object, though its appearance may be constructed from multiple overlays
     * (e.g. geometric elements, handles for manipulation, etc.).
     */
    interface MapFeature {
        /**
         * Returns true if the given marker belongs to this feature.
         */
        boolean ownsMarker(Marker marker);

        /**
         * Returns true if the given polyline belongs to this feature.
         */
        boolean ownsPolyline(Polyline polyline);

        boolean ownsPolygon(Polygon polygon);

        /**
         * Updates the feature's geometry after any UI handles have moved.
         */
        void update();

        /**
         * Removes the feature from the map, leaving it no longer usable.
         */
        void dispose();
    }

    /**
     * A marker that can optionally be dragged by the user.
     */
    private class MarkerFeature implements MapFeature {
        final MapView map;
        Marker marker;

        MarkerFeature(MapView map, MarkerDescription markerDescription) {
            this.map = map;
            this.marker = createMarker(map, markerDescription);
        }

        public void setIcon(MarkerIconDescription markerIconDescription) {
            Context context = requireContext();
            Bitmap bitmap = toBitmap(markerIconDescription, context);
            Drawable drawable = toDrawable(bitmap, context.getResources());

            marker.setIcon(drawable);
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

        @Override
        public boolean ownsPolygon(Polygon polygon) {
            return false;
        }

        public void update() {
        }

        public void dispose() {
            map.getOverlays().remove(marker);
            marker = null;
        }
    }

    private interface LineFeature extends MapFeature {

        List<MapPoint> getPoints();
    }

    private class StaticPolyLineFeature implements LineFeature {
        final MapView map;
        final Polyline polyline;
        private final List<MapPoint> points;

        StaticPolyLineFeature(MapView map, LineDescription lineDescription) {
            this.map = map;
            polyline = new Polyline();
            polyline.setColor(lineDescription.getStrokeColor());
            polyline.setOnClickListener((clickedPolyline, mapView, eventPos) -> {
                int featureId = findFeature(clickedPolyline);
                if (featureClickListener != null && featureId != -1) {
                    featureClickListener.onFeature(featureId);
                    return true;  // consume the event
                }
                return false;
            });
            Paint paint = polyline.getPaint();
            paint.setStrokeWidth(lineDescription.getStrokeWidth());
            map.getOverlays().add(polyline);

            points = lineDescription.getPoints();
            List<GeoPoint> geoPoints = StreamSupport.stream(points.spliterator(), false).map(mapPoint -> new GeoPoint(mapPoint.latitude, mapPoint.longitude, mapPoint.altitude)).collect(Collectors.toList());
            polyline.setPoints(geoPoints);
            map.invalidate();
        }

        @Override
        public boolean ownsMarker(Marker givenMarker) {
            return false;
        }

        @Override
        public boolean ownsPolyline(Polyline givenPolyline) {
            return polyline.equals(givenPolyline);
        }

        @Override
        public boolean ownsPolygon(Polygon polygon) {
            return false;
        }

        @Override
        public void update() {
        }

        @Override
        public void dispose() {
            map.getOverlays().remove(polyline);
        }

        @Override
        public List<MapPoint> getPoints() {
            return points;
        }
    }

    private class DynamicPolyLineFeature implements LineFeature {
        final MapView map;
        final List<Marker> markers = new ArrayList<>();
        final Polyline polyline;

        DynamicPolyLineFeature(MapView map, LineDescription lineDescription) {
            this.map = map;
            polyline = new Polyline();
            polyline.setColor(lineDescription.getStrokeColor());
            polyline.setOnClickListener((clickedPolyline, mapView, eventPos) -> {
                int featureId = findFeature(clickedPolyline);
                if (featureClickListener != null && featureId != -1) {
                    featureClickListener.onFeature(featureId);
                    return true;  // consume the event
                }
                return false;
            });
            Paint paint = polyline.getPaint();
            paint.setStrokeWidth(lineDescription.getStrokeWidth());
            map.getOverlays().add(polyline);

            List<MarkerDescription> markerDescriptions = getMarkersForPoints(lineDescription);
            for (MarkerDescription markerDescription : markerDescriptions) {
                markers.add(createMarker(map, markerDescription));
            }
            update();
        }

        @Override
        public boolean ownsMarker(Marker givenMarker) {
            return markers.contains(givenMarker);
        }

        @Override
        public boolean ownsPolyline(Polyline givenPolyline) {
            return polyline.equals(givenPolyline);
        }

        @Override
        public boolean ownsPolygon(Polygon polygon) {
            return false;
        }

        @Override
        public void update() {
            List<GeoPoint> geoPoints = new ArrayList<>();
            for (Marker marker : markers) {
                geoPoints.add(marker.getPosition());
            }

            polyline.setPoints(geoPoints);
            map.invalidate();
        }

        @Override
        public void dispose() {
            for (Marker marker : markers) {
                map.getOverlays().remove(marker);
            }
            markers.clear();
            map.getOverlays().remove(polyline);
        }

        @Override
        public List<MapPoint> getPoints() {
            List<MapPoint> points = new ArrayList<>();
            for (Marker marker : markers) {
                points.add(fromMarker(marker));
            }
            return points;
        }
    }

    private class DynamicPolygonFeature implements LineFeature {

        final MapView map;
        final List<Marker> markers = new ArrayList<>();
        final Polygon polygon;

        DynamicPolygonFeature(MapView map, PolygonDescription polygonDescription) {
            this.map = map;
            polygon = new Polygon();
            polygon.setStrokeColor(polygonDescription.getStrokeColor());
            polygon.setStrokeWidth(polygonDescription.getStrokeWidth());
            polygon.getFillPaint().setColor(polygonDescription.getFillColor());
            polygon.setOnClickListener((clickedPolygon, mapView, eventPos) -> {
                int featureId = findFeature(clickedPolygon);
                if (featureClickListener != null && featureId != -1) {
                    featureClickListener.onFeature(featureId);
                    return true;  // consume the event
                }
                return false;
            });

            map.getOverlays().add(polygon);

            List<MarkerDescription> markerDescriptions = getMarkersForPoints(polygonDescription);
            for (MarkerDescription markerDescription : markerDescriptions) {
                markers.add(createMarker(map, markerDescription));
            }
            update();
        }

        @Override
        public boolean ownsMarker(Marker givenMarker) {
            return markers.contains(givenMarker);
        }

        @Override
        public boolean ownsPolyline(Polyline other) {
            return false;
        }

        @Override
        public boolean ownsPolygon(Polygon other) {
            return polygon.equals(other);
        }

        @Override
        public void update() {
            List<GeoPoint> geoPoints = new ArrayList<>();
            for (Marker marker : markers) {
                geoPoints.add(marker.getPosition());
            }

            polygon.setPoints(geoPoints);
            map.invalidate();
        }

        @Override
        public void dispose() {
            for (Marker marker : markers) {
                map.getOverlays().remove(marker);
            }
            markers.clear();
            map.getOverlays().remove(polygon);
        }

        @Override
        public List<MapPoint> getPoints() {
            List<MapPoint> points = new ArrayList<>();
            for (Marker marker : markers) {
                points.add(fromMarker(marker));
            }
            return points;
        }
    }

    private class StaticPolygonFeature implements LineFeature {
        private final MapView map;
        @NonNull
        private final PolygonDescription polygonDescription;
        private final Polygon polygon = new Polygon();

        StaticPolygonFeature(MapView map, PolygonDescription polygonDescription) {
            this.map = map;
            this.polygonDescription = polygonDescription;

            map.getOverlays().add(polygon);
            polygon.getOutlinePaint().setColor(polygonDescription.getStrokeColor());
            polygon.setStrokeWidth(polygonDescription.getStrokeWidth());
            polygon.getFillPaint().setColor(polygonDescription.getFillColor());
            polygon.setPoints(StreamSupport.stream(polygonDescription.getPoints().spliterator(), false).map(point -> new GeoPoint(point.latitude, point.longitude)).collect(Collectors.toList()));
            polygon.setOnClickListener((polygon, mapView, eventPos) -> {
                int featureId = findFeature(polygon);
                if (featureClickListener != null && featureId != -1) {
                    featureClickListener.onFeature(featureId);
                    return true;  // consume the event
                }

                return false;
            });
        }

        @Override
        public boolean ownsMarker(Marker marker) {
            return false;
        }

        @Override
        public boolean ownsPolyline(Polyline polyline) {
            return false;
        }

        @Override
        public boolean ownsPolygon(Polygon polygon) {
            return polygon.equals(this.polygon);
        }

        @Override
        public void update() {
        }

        @Override
        public void dispose() {
            map.getOverlays().remove(polygon);
        }

        @Override
        public List<MapPoint> getPoints() {
            return polygonDescription.getPoints();
        }
    }

    /**
     * An overlay that draws an attribution message in the lower-right corner.
     */
    private static class AttributionOverlay extends Overlay {
        public static final int FONT_SIZE_DP = 12;
        public static final int MARGIN_DP = 10;

        private final Paint paint;

        AttributionOverlay(Context context) {
            super();

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(ContextExt.getThemeAttributeValue(context, com.google.android.material.R.attr.colorOnSurface));
            paint.setTextSize(FONT_SIZE_DP *
                    context.getResources().getDisplayMetrics().density);
            paint.setTextAlign(Paint.Align.RIGHT);
        }

        @Override
        public void draw(Canvas canvas, MapView map, boolean shadow) {
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

    private static class RegisterReceiver implements IRegisterReceiver {

        private final Context context;

        RegisterReceiver(Context context) {
            this.context = context;
        }

        @Override
        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
            return context != null ? context.registerReceiver(receiver, filter) : null;
        }

        @Override
        public void unregisterReceiver(BroadcastReceiver receiver) {
            if (context != null) {
                context.unregisterReceiver(receiver);
            }
        }

        @Override
        public void destroy() {
        }
    }

    private static class MapEventsReceiver implements org.osmdroid.events.MapEventsReceiver {

        private final PointListener clickListener;
        private final PointListener longPressListener;

        MapEventsReceiver(PointListener clickListener, PointListener longPressListener) {
            this.clickListener = clickListener;
            this.longPressListener = longPressListener;
        }

        @Override
        public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
            if (clickListener != null) {
                clickListener.onPoint(fromGeoPoint(geoPoint));
                return true;
            }
            return false;
        }

        @Override
        public boolean longPressHelper(GeoPoint geoPoint) {
            if (longPressListener != null) {
                longPressListener.onPoint(fromGeoPoint(geoPoint));
                return true;
            }
            return false;
        }
    }
}
