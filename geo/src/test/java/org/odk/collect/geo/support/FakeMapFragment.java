package org.odk.collect.geo.support;

import static java.util.Collections.emptyList;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import org.odk.collect.geo.maps.MapFragment;
import org.odk.collect.geo.maps.MapPoint;

import java.util.List;

public class FakeMapFragment implements MapFragment {

    private PointListener pointListener;
    private String locationProvider;
    private boolean retainMockAccuracy;

    @Override
    public void applyConfig(Bundle config) {

    }

    @Override
    public void addTo(@NonNull FragmentActivity activity, int containerId, @Nullable ReadyListener readyListener, @Nullable ErrorListener errorListener) {
        readyListener.onReady(this);
    }

    @NonNull
    @Override
    public MapPoint getCenter() {
        return null;
    }

    @Override
    public double getZoom() {
        return 0;
    }

    @Override
    public void setCenter(@Nullable MapPoint center, boolean animate) {

    }

    @Override
    public void zoomToPoint(@Nullable MapPoint center, boolean animate) {

    }

    @Override
    public void zoomToPoint(@Nullable MapPoint center, double zoom, boolean animate) {

    }

    @Override
    public void zoomToBoundingBox(Iterable<MapPoint> points, double scaleFactor, boolean animate) {

    }

    @Override
    public int addMarker(MapPoint point, boolean draggable, String iconAnchor) {
        return 0;
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
        return null;
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
        this.pointListener = listener;
    }

    @Override
    public void setRetainMockAccuracy(boolean retainMockAccuracy) {
        this.retainMockAccuracy = retainMockAccuracy;
    }

    public void setLocation(MapPoint mapPoint) {
        if (pointListener != null) {
            pointListener.onPoint(mapPoint);
        }
    }

    public void setLocationProvider(String locationProvider) {
        this.locationProvider = locationProvider;
    }

    public boolean isRetainMockAccuracy() {
        return retainMockAccuracy;
    }
}
