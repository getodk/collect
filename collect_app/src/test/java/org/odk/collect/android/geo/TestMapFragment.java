package org.odk.collect.android.geo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import org.odk.collect.geo.maps.MapFragment;
import org.odk.collect.geo.maps.MapPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMapFragment implements MapFragment {
    private int zoomCount;

    private boolean animate;
    private MapPoint zoomPoint;
    private Iterable<MapPoint> zoomBoundingBox;
    private double scaleFactor;
    private double zoomLevel;
    private PointListener locationListener;

    private int lastFeatureId;
    private final Map<MapPoint, Integer> mappedPoints = new HashMap<>();

    public int getZoomCount() {
        return zoomCount;
    }

    public boolean wasLatestZoomCallAnimated() {
        return animate;
    }

    public MapPoint getLatestZoomPoint() {
        return zoomPoint;
    }

    public Iterable<MapPoint> getLatestZoomBoundingBox() {
        return zoomBoundingBox;
    }

    public double getLatestScaleFactor() {
        return scaleFactor;
    }

    public void onLocationChanged(MapPoint point) {
        if (locationListener != null) {
            locationListener.onPoint(point);
        }
    }

    public int getMappedPointCount() {
        return mappedPoints.size();
    }

    public boolean isMapped(MapPoint point) {
        return mappedPoints.containsKey(point);
    }

    public int getFeatureIdFor(@NonNull MapPoint point) {
        return mappedPoints.get(point);
    }

    public void resetState() {
        zoomCount = 0;
        animate = false;
        zoomPoint = null;
        zoomBoundingBox = null;
        scaleFactor = 0;
        zoomLevel = 0;
        locationListener = null;
        lastFeatureId = 0;
        mappedPoints.clear();
    }

    @Override
    public void applyConfig(Bundle config) {

    }

    @Override
    public void addTo(FragmentManager fragmentManager, int containerId, @Nullable ReadyListener readyListener, @Nullable ErrorListener errorListener) {
        readyListener.onReady(this);
    }

    @NonNull
    @Override
    public MapPoint getCenter() {
        return zoomPoint;
    }

    @Override
    public double getZoom() {
        return zoomLevel;
    }

    @Override
    public void setCenter(@Nullable MapPoint center, boolean animate) {

    }

    @Override
    public void zoomToPoint(@Nullable MapPoint center, boolean animate) {
        this.zoomPoint = center;
        this.animate = animate;
        this.zoomBoundingBox = null;

        zoomCount++;
    }

    @Override
    public void zoomToPoint(@Nullable MapPoint center, double zoom, boolean animate) {
        zoomToPoint(center, animate);
        this.zoomLevel = zoom;
    }

    @Override
    public void zoomToBoundingBox(Iterable<MapPoint> points, double scaleFactor, boolean animate) {
        this.zoomBoundingBox = points;
        this.scaleFactor = scaleFactor;
        this.animate = animate;

        zoomCount++;
    }

    @Override
    public int addMarker(MapPoint point, boolean draggable, @IconAnchor String iconAnchor) {
        mappedPoints.put(point, lastFeatureId);

        return lastFeatureId++;
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
        return null;
    }

    @Override
    public void removeFeature(int featureId) {

    }

    @Override
    public void clearFeatures() {
        mappedPoints.clear();
    }

    @Override
    public void setClickListener(@Nullable PointListener listener) {

    }

    @Override
    public void setLongPressListener(@Nullable PointListener listener) {

    }

    @Override
    public void setFeatureClickListener(@Nullable FeatureListener listener) {

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
        return new MapPoint(5, 5);
    }

    @Nullable
    @Override
    public String getLocationProvider() {
        return null;
    }

    @Override
    public void runOnGpsLocationReady(@NonNull ReadyListener listener) {

    }

    @Override
    public void setGpsLocationListener(@Nullable PointListener listener) {
        locationListener = listener;
    }

    @Override
    public void setRetainMockAccuracy(boolean retainMockAccuracy) {

    }
}
