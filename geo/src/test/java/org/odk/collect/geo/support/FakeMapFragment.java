package org.odk.collect.geo.support;

import static java.util.Collections.emptyList;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import org.odk.collect.geo.maps.MapFragment;
import org.odk.collect.geo.maps.MapPoint;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;

public class FakeMapFragment implements MapFragment {

    private PointListener gpsLocationListener;
    private String locationProvider;
    private boolean retainMockAccuracy;
    private MapPoint center;
    private double zoom;
    private Pair<Iterable<MapPoint>, Float> zoomBoundingBox;
    private MapPoint gpsLocation;
    private FeatureListener featureClickListener;
    private final List<MapPoint> markers = new ArrayList<>();

    public void applyConfig(Bundle config) {

    }

    @Override
    public void addTo(FragmentManager fragmentManager, int containerId, @Nullable ReadyListener readyListener, @Nullable ErrorListener errorListener) {
        readyListener.onReady(this);
    }

    @NonNull
    @Override
    public MapPoint getCenter() {
        return center;
    }

    @Override
    public double getZoom() {
        return zoom;
    }

    @Override
    public void setCenter(@Nullable MapPoint center, boolean animate) {
        this.center = center;
    }

    @Override
    public void zoomToPoint(@Nullable MapPoint center, boolean animate) {
        this.center = center;
    }

    @Override
    public void zoomToPoint(@Nullable MapPoint center, double zoom, boolean animate) {
        zoomBoundingBox = null;

        this.center = center;
        this.zoom = zoom;
    }

    @Override
    public void zoomToBoundingBox(Iterable<MapPoint> points, double scaleFactor, boolean animate) {
        center = null;
        zoom = 0;

        zoomBoundingBox = new Pair(points, scaleFactor);
    }

    @Override
    public int addMarker(MapPoint point, boolean draggable, String iconAnchor) {
        markers.add(point);
        return markers.size() - 1;
    }

    @Override
    public void setMarkerIcon(int featureId, int drawableId) {

    }

    @Override
    public MapPoint getMarkerPoint(int featureId) {
        return null;
    }

    @Override
    public int addDraggablePoly(@NonNull Iterable<MapPoint> points, boolean closedPolygon) {
        return 0;
    }

    @Override
    public void appendPointToPoly(int featureId, @NonNull MapPoint point) {

    }

    @Override
    public void removePolyLastPoint(int featureId) {

    }

    @NonNull
    @Override
    public List<MapPoint> getPolyPoints(int featureId) {
        return emptyList();
    }

    @Override
    public void removeFeature(int featureId) {

    }

    @Override
    public void clearFeatures() {
        markers.clear();
    }

    @Override
    public void setClickListener(@Nullable PointListener listener) {

    }

    @Override
    public void setLongPressListener(@Nullable PointListener listener) {

    }

    @Override
    public void setFeatureClickListener(@Nullable FeatureListener listener) {
        featureClickListener = listener;
    }

    @Override
    public void setDragEndListener(@Nullable FeatureListener listener) {

    }

    @Override
    public void setGpsLocationEnabled(boolean enabled) {

    }

    @Nullable
    @Override
    public MapPoint getGpsLocation() {
        return gpsLocation;
    }

    @Nullable
    @Override
    public String getLocationProvider() {
        return locationProvider;
    }

    @Override
    public void runOnGpsLocationReady(@NonNull ReadyListener listener) {

    }

    @Override
    public void setGpsLocationListener(@Nullable PointListener listener) {
        this.gpsLocationListener = listener;
    }

    @Override
    public void setRetainMockAccuracy(boolean retainMockAccuracy) {
        this.retainMockAccuracy = retainMockAccuracy;
    }

    public void setLocation(MapPoint mapPoint) {
        gpsLocation = mapPoint;

        if (gpsLocationListener != null) {
            gpsLocationListener.onPoint(mapPoint);
        }
    }

    public void setLocationProvider(String locationProvider) {
        this.locationProvider = locationProvider;
    }

    public boolean isRetainMockAccuracy() {
        return retainMockAccuracy;
    }

    public Pair<Iterable<MapPoint>, Float> getZoomBoundingBox() {
        return zoomBoundingBox;
    }

    public void clickOnFeature(int index) {
        featureClickListener.onFeature(index);
    }

    public List<MapPoint> getMarkers() {
        return markers;
    }
}
