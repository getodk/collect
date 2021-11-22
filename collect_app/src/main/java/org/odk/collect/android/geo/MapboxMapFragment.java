package org.odk.collect.android.geo;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.backgroundColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.LocationListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnLineClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.BackgroundLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.layers.TransitionOptions;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.geo.MbtilesFile.LayerType;
import org.odk.collect.android.geo.MbtilesFile.MbtilesException;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.location.client.MapboxLocationCallback;
import org.odk.collect.android.utilities.MapFragmentReferenceLayerUtils;
import org.odk.collect.geo.maps.MapFragment;
import org.odk.collect.geo.maps.MapPoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

public class MapboxMapFragment extends org.odk.collect.android.geo.mapboxsdk.MapFragment
    implements MapFragment, OnMapReadyCallback,
    MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener, LocationListener {

    private static final long LOCATION_INTERVAL_MILLIS = 1000;
    private static final long LOCATION_MAX_WAIT_MILLIS = 5000;
    private static final LocationEngineRequest LOCATION_REQUEST =
        new LocationEngineRequest.Builder(LOCATION_INTERVAL_MILLIS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(LOCATION_MAX_WAIT_MILLIS)
            .build();

    // Bundle keys understood by applyConfig().
    static final String KEY_STYLE_URL = "STYLE_URL";

    @Inject
    MapProvider mapProvider;

    @Inject
    ReferenceLayerRepository referenceLayerRepository;

    private MapboxMap map;
    private ReadyListener mapReadyListener;
    private final List<ReadyListener> gpsLocationReadyListeners = new ArrayList<>();
    private PointListener gpsLocationListener;
    private PointListener clickListener;
    private PointListener longPressListener;
    private FeatureListener featureClickListener;
    private FeatureListener dragEndListener;

    private LocationComponent locationComponent;
    private boolean clientWantsLocationUpdates;
    private MapPoint lastLocationFix;

    private int nextFeatureId = 1;
    private final Map<Integer, MapFeature> features = new HashMap<>();
    private SymbolManager symbolManager;
    private LineManager lineManager;
    private boolean isDragging;
    private String styleUrl = Style.MAPBOX_STREETS;
    private File referenceLayerFile;
    private final List<Source> overlaySources = new ArrayList<>();
    private final MapboxLocationCallback locationCallback = new MapboxLocationCallback(this);
    private static String lastLocationProvider;

    private TileHttpServer tileServer;

    private static final String PLACEHOLDER_LAYER_ID = "placeholder";

    // During Robolectric tests, Google Play Services is unavailable; sadly, the
    // "map" field will be null and many operations will need to be stubbed out.
    @VisibleForTesting public static boolean testMode;

    @Override public void addTo(
        @NonNull FragmentActivity activity, int containerId,
        @Nullable ReadyListener readyListener, @Nullable ErrorListener errorListener) {
        Context context = getContext();
        mapReadyListener = readyListener;
        if (MapboxUtils.initMapbox() == null) {
            MapProvider.getConfigurator().showUnavailableMessage(context);
            if (errorListener != null) {
                errorListener.onError();
            }
            return;
        }

        // Mapbox SDK only knows how to fetch tiles via HTTP.  If we want it to
        // display tiles from a local file, we have to serve them locally over HTTP.
        try {
            tileServer = new TileHttpServer();
            tileServer.start();
        } catch (IOException e) {
            Timber.e(e, "Could not start the TileHttpServer");
        }

        // If the containing activity is being re-created upon screen rotation,
        // the FragmentManager will have also re-created a copy of the previous
        // MapboxMapFragment.  We don't want these useless copies of old fragments
        // to linger, so the following line calls .replace() instead of .add().
        activity.getSupportFragmentManager()
            .beginTransaction().replace(containerId, this).commitNow();
        getMapAsync(map -> {
            this.map = map;  // signature of getMapAsync() ensures map is never null

            map.setStyle(getStyleBuilder(), style -> {
                map.getUiSettings().setCompassGravity(Gravity.TOP | Gravity.START);
                map.getUiSettings().setCompassMargins(36, 36, 36, 36);

                map.addOnMapClickListener(this);
                map.addOnMapLongClickListener(this);

                // MAPBOX ISSUE: https://github.com/mapbox/mapbox-gl-native/issues/15262
                // Unfortunately, the API no longer provides a way to to get an ID
                // or a reference to the symbol layer or the line layer.  We needed
                // this in order to keep the symbol layer and line layer on top when
                // adding a reference layer to the map.  But without a way to refer
                // to these layers, we can't move them to the top or insert the
                // reference layer below them.  To work around this, we add a dummy
                // placeholder layer first, so that the symbol, line, and location
                // layers are added on top of it.  Then we use the placeholder layer
                // to determine where to insert the reference layer.
                style.addLayer(new BackgroundLayer(PLACEHOLDER_LAYER_ID)
                    .withProperties(backgroundColor("rgba(0, 0, 0, 0)")));

                // MAPBOX ISSUE: https://github.com/mapbox/mapbox-plugins-android/issues/863
                // Only the last-created manager gets draggable annotations. For
                // symbols to be draggable, the SymbolManager must be created last.
                lineManager = createLineManager();
                symbolManager = createSymbolManager();

                loadReferenceOverlay();
                initLocationComponent();

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    toLatLng(INITIAL_CENTER), INITIAL_ZOOM));

                // If the screen is rotated before the map is ready, this fragment
                // could already be detached, which makes it unsafe to use.  Only
                // call the ReadyListener if this fragment is still attached.
                if (mapReadyListener != null && getActivity() != null) {
                    mapReadyListener.onReady(this);
                }
            });

            // In Robolectric tests, getMapAsync() never gets around to calling its
            // callback; we have to invoke the ready listener directly.
            if (testMode && mapReadyListener != null) {
                mapReadyListener.onReady(this);
            }
        });
    }

    @Override public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (map != null) {
            lastLocationFix = fromLocation(location);
            lastLocationProvider = location != null ? location.getProvider() : null;
            Timber.i("Received location update: %s (%s)", lastLocationFix, lastLocationProvider);
            if (locationComponent != null) {
                locationComponent.forceLocationUpdate(location);
            }
            for (ReadyListener listener : gpsLocationReadyListeners) {
                listener.onReady(this);
            }
            gpsLocationReadyListeners.clear();
            if (gpsLocationListener != null) {
                gpsLocationListener.onPoint(lastLocationFix);
            }
        }
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
        if (tileServer != null) {
            tileServer.destroy();
        }
        super.onDestroy();
    }

    @Override public void applyConfig(Bundle config) {
        styleUrl = config.getString(KEY_STYLE_URL);
        referenceLayerFile = MapFragmentReferenceLayerUtils.getReferenceLayerFile(config, referenceLayerRepository);
        if (map != null) {
            resetLocationComponent();

            map.setStyle(getStyleBuilder(), style -> {
                // See addTo() above for why we add this placeholder layer.
                style.addLayer(new BackgroundLayer(PLACEHOLDER_LAYER_ID)
                    .withProperties(backgroundColor("rgba(0, 0, 0, 0)")));
                lineManager = createLineManager();
                symbolManager = createSymbolManager();

                loadReferenceOverlay();
                initLocationComponent();

                if (mapReadyListener != null && getActivity() != null) {
                    mapReadyListener.onReady(this);
                }
            });
        }
    }

    /**
     * Reset the location component so that it is not tied to the placeholder layer.
     * We need to do this before re-setting map style to avoid exceptions.
     */
    @SuppressLint("MissingPermission")
    private void resetLocationComponent() {
        map.getLocationComponent().setLocationComponentEnabled(false);
        map.getLocationComponent().activateLocationComponent(LocationComponentActivationOptions.builder(getContext(), map.getStyle()).build());
    }

    @Override public @NonNull MapPoint getCenter() {
        if (map == null) {  // during Robolectric tests, map will be null
            return INITIAL_CENTER;
        }
        return fromLatLng(map.getCameraPosition().target);
    }

    @Override public double getZoom() {
        if (map == null) {  // during Robolectric tests, map will be null
            return INITIAL_ZOOM;
        }
        return map.getCameraPosition().zoom;
    }

    @Override public void setCenter(@Nullable MapPoint center, boolean animate) {
        if (map == null) {  // during Robolectric tests, map will be null
            return;
        }
        if (center != null) {
            moveOrAnimateCamera(CameraUpdateFactory.newLatLng(toLatLng(center)), animate);
        }
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

    @Override public int addMarker(MapPoint point, boolean draggable, @IconAnchor String iconAnchor) {
        int featureId = nextFeatureId++;
        features.put(featureId, new MarkerFeature(featureId, symbolManager, point, draggable, iconAnchor));
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
        return feature instanceof MarkerFeature ?
            ((MarkerFeature) feature).getPoint() : null;
    }

    @Override public @NonNull List<MapPoint> getPolyPoints(int featureId) {
        MapFeature feature = features.get(featureId);
        if (feature instanceof PolyFeature) {
            return ((PolyFeature) feature).getPoints();
        }
        return new ArrayList<>();
    }

    @Override public void setFeatureClickListener(@Nullable FeatureListener listener) {
        featureClickListener = listener;
    }

    @Override public void setDragEndListener(@Nullable FeatureListener listener) {
        dragEndListener = listener;
    }

    @Override public @Nullable String getLocationProvider() {
        return lastLocationProvider;
    }

    @Override public boolean onMapClick(@NonNull LatLng point) {
        // MAPBOX ISSUE: Dragging can also generate map click events, and the
        // Mapbox SDK seems to provide no way to prevent drag touches from being
        // passed through to the map and being interpreted as a map click.
        // Our workaround is to track drags in progress with the isDragging flag.
        if (clickListener != null && !isDragging) {
            clickListener.onPoint(fromLatLng(point));
        }

        // MAPBOX ISSUE: Unfortunately, onMapClick is called before onAnnotationClick,
        // which means that every click on a marker will also cause a click event on
        // the map.  Returning true will consume the event and prevent the marker's
        // onAnnotationClick from ever being called, so we have to return false.
        return false;
    }

    @Override public boolean onMapLongClick(@NonNull LatLng latLng) {
        // MAPBOX ISSUE: Dragging can also generate map long-click events, and the
        // Mapbox SDK seems to provide no way to prevent drag touches from being
        // passed through to the map and being interpreted as a map click.
        // Our workaround is to track drags in progress with the isDragging flag.
        if (longPressListener != null && !isDragging) {
            longPressListener.onPoint(fromLatLng(latLng));
        }
        return true;
    }

    @Override public int addDraggablePoly(@NonNull Iterable<MapPoint> points, boolean closedPolygon) {
        int featureId = nextFeatureId++;
        features.put(featureId, new PolyFeature(featureId, lineManager, symbolManager, points, closedPolygon));
        return featureId;
    }

    @Override public void appendPointToPoly(int featureId, @NonNull MapPoint point) {
        MapFeature feature = features.get(featureId);
        if (feature instanceof PolyFeature) {
            ((PolyFeature) feature).appendPoint(point);
        }
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
        nextFeatureId = 1;
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

    @Override
    public void setRetainMockAccuracy(boolean retainMockAccuracy) {
        locationCallback.setRetainMockAccuracy(retainMockAccuracy);
    }

    @Override public void setGpsLocationEnabled(boolean enable) {
        if (enable != clientWantsLocationUpdates) {
            clientWantsLocationUpdates = enable;
            enableLocationUpdates(clientWantsLocationUpdates);
        }
    }

    @Override public void runOnGpsLocationReady(@NonNull ReadyListener listener) {
        if (lastLocationFix != null) {
            listener.onReady(this);
        } else {
            gpsLocationReadyListeners.add(listener);
        }
    }

    @Override public @Nullable MapPoint getGpsLocation() {
        return lastLocationFix;
    }

    private static @NonNull MapPoint fromLatLng(@NonNull LatLng latLng) {
        return new MapPoint(latLng.getLatitude(), latLng.getLongitude());
    }

    private static @Nullable MapPoint fromLocation(@Nullable Location location) {
        if (location == null) {
            return null;
        }
        return new MapPoint(location.getLatitude(), location.getLongitude(),
            location.getAltitude(), location.getAccuracy());
    }

    private static @NonNull MapPoint fromSymbol(@NonNull Symbol symbol, double alt, double sd) {
        LatLng position = symbol.getLatLng();
        return new MapPoint(position.getLatitude(), position.getLongitude(), alt, sd);
    }

    private static @NonNull LatLng toLatLng(@NonNull MapPoint point) {
        return new LatLng(point.lat, point.lon);
    }

    private Symbol createSymbol(SymbolManager symbolManager, MapPoint point, boolean draggable, @IconAnchor String iconAnchor) {
        return symbolManager.create(new SymbolOptions()
            .withLatLng(toLatLng(point))
            .withIconImage(addIconImage(R.drawable.ic_map_point))
            .withIconSize(1f)
            .withSymbolSortKey(10f)
            .withDraggable(draggable)
            .withTextOpacity(0f)
            .withIconAnchor(getIconAnchorValue(iconAnchor))
        );
    }

    private String getIconAnchorValue(@IconAnchor String iconAnchor) {
        switch (iconAnchor) {
            case BOTTOM:
                return Property.ICON_ANCHOR_BOTTOM;
            default:
                return Property.ICON_ANCHOR_CENTER;
        }
    }

    private void moveOrAnimateCamera(CameraUpdate movement, boolean animate) {
        if (animate) {
            map.animateCamera(movement);
        } else {
            map.moveCamera(movement);
        }
    }

    private LatLngBounds expandBounds(LatLngBounds bounds, double factor) {
        double north = bounds.getNorthEast().getLatitude();
        double south = bounds.getSouthWest().getLatitude();
        double latCenter = (north + south) / 2;
        double latRadius = ((north - south) / 2) * factor;
        north = Math.min(90, latCenter + latRadius);
        south = Math.max(-90, latCenter - latRadius);

        double east = bounds.getNorthEast().getLongitude();
        double west = bounds.getSouthWest().getLongitude();
        while (east < west) {
            east += 360;
        }
        double lonCenter = (east + west) / 2;
        double lonRadius = Math.min(180 - 1e-6, ((east - west) / 2) * factor);
        east = lonCenter + lonRadius;
        west = lonCenter - lonRadius;

        return new LatLngBounds.Builder()
            .include(new LatLng(south, west))
            .include(new LatLng(north, east))
            .build();
    }

    private Style.Builder getStyleBuilder() {
        return getBasemapStyleBuilder().withTransition(new TransitionOptions(0, 0, false));
    }

    private Style.Builder getBasemapStyleBuilder() {
        if (BuildConfig.MAPBOX_ACCESS_TOKEN.isEmpty()) {
            // When the MAPBOX_ACCESS_TOKEN is missing, any attempt to load
            // map data from Mapbox will cause the Mapbox SDK to abort with an
            // uncatchable assertion failure, so we have to be careful to avoid
            // requesting any Mapbox-sourced layers!  Since we can't use the
            // Mapbox base map, we fall back to the OSM raster base map.
            TileSet tiles = new TileSet("2.2.0", "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png");
            return new Style.Builder()
                .withSource(new RasterSource("[osm]", tiles, 256))
                .withLayer(new RasterLayer("[osm]", "[osm]"));
        }
        return new Style.Builder().fromUrl(styleUrl);
    }

    /**
     * Updates the map to reflect the value of referenceLayerFile.  Because this
     * involves adding things to the map's Style, call this only after the Style
     * is fully loaded, in setStyle()'s OnStyleLoaded callback.
     */
    private void loadReferenceOverlay() {
        if (referenceLayerFile != null) {
            addMbtiles(referenceLayerFile.getName(), referenceLayerFile);
        }
    }

    @SuppressWarnings("TimberExceptionLogging")
    private void addMbtiles(String id, File file) {
        MbtilesFile mbtiles;
        try {
            mbtiles = new MbtilesFile(file);
        } catch (MbtilesException e) {
            Timber.w(e.getMessage());
            return;
        }

        TileSet tileSet = createTileSet(mbtiles, tileServer.getUrlTemplate(id));
        tileServer.addSource(id, mbtiles);

        if (mbtiles.getLayerType() == LayerType.VECTOR) {
            addOverlaySource(new VectorSource(id, tileSet));
            List<MbtilesFile.VectorLayer> layers = mbtiles.getVectorLayers();
            for (MbtilesFile.VectorLayer layer : layers) {
                // Pick a colour that's a function of the filename and layer name.
                // The colour will appear essentially random; the only purpose here
                // is to try to assign different colours to different layers, such
                // that each individual layer appears in its own consistent colour.
                int hue = (((id + "." + layer.name).hashCode()) & 0x7fffffff) % 360;
                addOverlayLayer(new LineLayer(id + "/" + layer.name, id).withProperties(
                    lineColor(Color.HSVToColor(new float[] {hue, 0.7f, 1})),
                    lineWidth(1f),
                    lineOpacity(0.7f)
                ).withSourceLayer(layer.name));
            }
        }
        if (mbtiles.getLayerType() == LayerType.RASTER) {
            addOverlaySource(new RasterSource(id, tileSet));
            addOverlayLayer(new RasterLayer(id + ".raster", id));
        }
        Timber.i("Added %s as a %s layer at /%s", file, mbtiles.getLayerType(), id);
    }

    @SuppressWarnings("TimberExceptionLogging")
    private TileSet createTileSet(MbtilesFile mbtiles, String urlTemplate) {
        TileSet tileSet = new TileSet("2.2.0", urlTemplate);

        // Configure the TileSet using the metadata in the .mbtiles file.
        try {
            tileSet.setName(mbtiles.getMetadata("name"));
            try {
                tileSet.setMinZoom(Integer.parseInt(mbtiles.getMetadata("minzoom")));
                tileSet.setMaxZoom(Integer.parseInt(mbtiles.getMetadata("maxzoom")));
            } catch (NumberFormatException e) { /* ignore */ }

            String[] parts = mbtiles.getMetadata("center").split(",");
            if (parts.length == 3) {  // latitude, longitude, zoom
                try {
                    tileSet.setCenter(
                        Float.parseFloat(parts[0]), Float.parseFloat(parts[1]),
                        (float) Integer.parseInt(parts[2])
                    );
                } catch (NumberFormatException e) { /* ignore */ }
            }

            parts = mbtiles.getMetadata("bounds").split(",");
            if (parts.length == 4) {  // left, bottom, right, top
                try {
                    tileSet.setBounds(
                        Float.parseFloat(parts[0]), Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2]), Float.parseFloat(parts[3])
                    );
                } catch (NumberFormatException e) { /* ignore */ }
            }
        } catch (MbtilesException e) {
            Timber.w(e.getMessage());
        }

        return tileSet;
    }

    private void addOverlayLayer(Layer layer) {
        // The overlay goes just below the placeholder layer, so it will be just
        // under the location, symbol, and line layers, but above everything else.
        map.getStyle().addLayerBelow(layer, PLACEHOLDER_LAYER_ID);
    }

    private void addOverlaySource(Source source) {
        map.getStyle().addSource(source);
        overlaySources.add(source);
    }

    /** Adds an image to the style unless it's already present, and returns its ID. */
    private String addIconImage(int drawableId) {
        String imageId = "icon-" + drawableId;
        map.getStyle(style -> {
            if (style.getImage(imageId) == null) {
                Drawable icon = ContextCompat.getDrawable(getContext(), drawableId);
                style.addImage(imageId, icon);
            }
        });
        return imageId;
    }

    private SymbolManager createSymbolManager() {
        SymbolManager symbolManager = new SymbolManager(getMapView(), map, map.getStyle());
        // Turning on allowOverlap and ignorePlacement causes the symbols to
        // always be shown (otherwise Mapbox hides them automatically when they
        // come too close to other symbols or labels on the map).
        symbolManager.setIconAllowOverlap(true);
        symbolManager.setIconIgnorePlacement(true);
        symbolManager.setIconPadding(0f);
        return symbolManager;
    }

    private LineManager createLineManager() {
        return new LineManager(getMapView(), map, map.getStyle());
    }

    private void initLocationComponent() {
        if (map == null || map.getStyle() == null) {  // map is null during Robolectric tests
            return;
        }

        LocationEngine engine = LocationEngineProvider.getBestLocationEngine(getContext());
        locationComponent = map.getLocationComponent();
        locationComponent.activateLocationComponent(
            LocationComponentActivationOptions.builder(getContext(), map.getStyle())
                .locationEngine(engine)
                .locationComponentOptions(
                    LocationComponentOptions.builder(getContext())
                        .layerAbove(PLACEHOLDER_LAYER_ID)
                        .foregroundDrawable(R.drawable.ic_crosshairs)
                        .backgroundDrawable(R.drawable.empty)
                        .enableStaleState(false)  // don't switch to other drawables
                        .elevation(0)  // remove the shadow
                        .build()
                )
                .build()
        );

        locationComponent.setCameraMode(CameraMode.NONE);
        locationComponent.setRenderMode(RenderMode.NORMAL);
        enableLocationUpdates(clientWantsLocationUpdates);
    }

    @SuppressWarnings({"MissingPermission"})  // permission checks for location services are handled in widgets
    private void enableLocationUpdates(boolean enable) {
        if (locationComponent != null) {
            LocationEngine engine = locationComponent.getLocationEngine();
            if (enable) {
                Timber.i("Requesting location updates from %s (to %s)", engine, this);
                engine.requestLocationUpdates(LOCATION_REQUEST, locationCallback, null);
                engine.getLastLocation(locationCallback);
            } else {
                Timber.i("Stopping location updates from %s (to %s)", engine, this);
                engine.removeLocationUpdates(locationCallback);
            }
            Timber.i("setLocationComponentEnabled to %s (for %s)", enable, locationComponent);
            locationComponent.setLocationComponentEnabled(enable);
        }
    }

    /**
     * A MapFeature is a physical feature on a map, such as a point, a road,
     * a building, a region, etc.  It is presented to the user as one editable
     * object, though its appearance may be constructed from multiple overlays
     * (e.g. geometric elements, handles for manipulation, etc.).
     */
    interface MapFeature {
        /** Removes the feature from the map, leaving it no longer usable. */
        void dispose();
    }

    /** A Symbol that can optionally be dragged by the user. */
    private class MarkerFeature implements MapFeature {
        private final int featureId;
        private final SymbolManager symbolManager;
        private final ClickListener clickListener = new ClickListener();
        private final DragListener dragListener = new DragListener();
        private MapPoint point;
        private Symbol symbol;

        MarkerFeature(int featureId, SymbolManager symbolManager, MapPoint point, boolean draggable, @IconAnchor String iconAnchor) {
            this.featureId = featureId;
            this.symbolManager = symbolManager;
            this.point = point;
            this.symbol = createSymbol(symbolManager, point, draggable, iconAnchor);
            symbolManager.addClickListener(clickListener);
            symbolManager.addDragListener(dragListener);
        }

        public void setIcon(int drawableId) {
            symbol.setIconImage(addIconImage(drawableId));
            symbolManager.update(symbol);
        }

        public MapPoint getPoint() {
            return point;
        }

        public void dispose() {
            symbolManager.removeClickListener(clickListener);
            symbolManager.removeDragListener(dragListener);
            symbolManager.delete(symbol);
            symbol = null;
        }

        class ClickListener implements OnSymbolClickListener {
            @Override public void onAnnotationClick(Symbol clickedSymbol) {
                if (clickedSymbol.getId() == symbol.getId() && featureClickListener != null) {
                    featureClickListener.onFeature(featureId);
                }
            }
        }

        class DragListener implements OnSymbolDragListener {
            @Override public void onAnnotationDragStarted(Symbol draggedSymbol) {
                isDragging = true;
            }

            @Override public void onAnnotationDrag(Symbol draggedSymbol) {
                isDragging = true;
                if (draggedSymbol.getId() == symbol.getId()) {
                    // When a symbol is manually dragged, the position is no longer
                    // obtained from a GPS reading, so the altitude and standard
                    // deviation fields are no longer meaningful; reset them to zero.
                    point = fromSymbol(symbol, 0, 0);
                }
            }

            @Override public void onAnnotationDragFinished(Symbol draggedSymbol) {
                onAnnotationDrag(draggedSymbol);
                if (draggedSymbol.getId() == symbol.getId() && dragEndListener != null) {
                    dragEndListener.onFeature(featureId);
                }
                isDragging = false;
            }
        }
    }

    /** A polyline or polygon that can be manipulated by dragging Symbols at its vertices. */
    private class PolyFeature implements MapFeature {
        public static final float STROKE_WIDTH = 5;

        private final int featureId;
        private final LineManager lineManager;
        private final SymbolManager symbolManager;
        private final SymbolClickListener symbolClickListener = new SymbolClickListener();
        private final LineClickListener lineClickListener = new LineClickListener();
        private final SymbolDragListener symbolDragListener = new SymbolDragListener();
        private final List<MapPoint> points = new ArrayList<>();
        private final List<Symbol> symbols = new ArrayList<>();
        private final boolean closedPolygon;
        private Line line;

        PolyFeature(int featureId, LineManager lineManager, SymbolManager symbolManager,
            Iterable<MapPoint> points, boolean closedPolygon) {
            this.featureId = featureId;
            this.lineManager = lineManager;
            this.symbolManager = symbolManager;
            this.closedPolygon = closedPolygon;
            for (MapPoint point : points) {
                this.points.add(point);
                this.symbols.add(createSymbol(symbolManager, point, true, CENTER));
            }
            line = lineManager.create(new LineOptions()
                .withLineColor(ColorUtils.colorToRgbaString(requireContext().getResources().getColor(R.color.mapLineColor)))
                .withLineWidth(STROKE_WIDTH)
                .withLatLngs(new ArrayList<>())
            );
            updateLine();
            symbolManager.addClickListener(symbolClickListener);
            symbolManager.addDragListener(symbolDragListener);
            lineManager.addClickListener(lineClickListener);
        }

        public List<MapPoint> getPoints() {
            return new ArrayList<>(points);
        }

        public void dispose() {
            symbolManager.removeClickListener(symbolClickListener);
            symbolManager.removeDragListener(symbolDragListener);
            lineManager.removeClickListener(lineClickListener);
            if (line != null) {
                lineManager.delete(line);
                line = null;
            }
            for (Symbol symbol : symbols) {
                symbolManager.delete(symbol);
            }
            symbols.clear();
        }

        public void appendPoint(MapPoint point) {
            if (map == null) {  // during Robolectric tests, map will be null
                return;
            }
            points.add(point);
            symbols.add(createSymbol(symbolManager, point, true, CENTER));
            updateLine();
        }

        public void removeLastPoint() {
            int last = points.size() - 1;
            if (last >= 0) {
                symbolManager.delete(symbols.get(last));
                symbols.remove(last);
                points.remove(last);
                updateLine();
            }
        }

        private void updateLine() {
            List<LatLng> latLngs = new ArrayList<>();
            for (MapPoint point : points) {
                latLngs.add(toLatLng(point));
            }
            if (closedPolygon && !latLngs.isEmpty()) {
                latLngs.add(latLngs.get(0));
            }
            line.setLatLngs(latLngs);
            lineManager.update(line);
        }

        class SymbolClickListener implements OnSymbolClickListener {
            @Override public void onAnnotationClick(Symbol clickedSymbol) {
                for (Symbol symbol : symbols) {
                    if (clickedSymbol.getId() == symbol.getId() && featureClickListener != null) {
                        featureClickListener.onFeature(featureId);
                        break;
                    }
                }
            }
        }

        class LineClickListener implements OnLineClickListener {
            @Override public void onAnnotationClick(Line clickedLine) {
                if (clickedLine.getId() == line.getId() && featureClickListener != null) {
                    featureClickListener.onFeature(featureId);
                }
            }
        }

        class SymbolDragListener implements OnSymbolDragListener {
            @Override public void onAnnotationDragStarted(Symbol draggedSymbol) {
                isDragging = true;
            }

            @Override public void onAnnotationDrag(Symbol draggedSymbol) {
                isDragging = true;
                for (int i = 0; i < symbols.size(); i++) {
                    Symbol symbol = symbols.get(i);
                    if (draggedSymbol.getId() == symbol.getId()) {
                        // When a symbol is manually dragged, the position is no longer
                        // obtained from a GPS reading, so the altitude and standard
                        // deviation fields are no longer meaningful; reset them to zero.
                        points.set(i, fromSymbol(symbol, 0, 0));
                    }
                }
                updateLine();
            }

            @Override public void onAnnotationDragFinished(Symbol draggedSymbol) {
                onAnnotationDrag(draggedSymbol);
                isDragging = false;
                if (dragEndListener != null) {
                    for (Symbol symbol : symbols) {
                        if (draggedSymbol.getId() == symbol.getId()) {
                            dragEndListener.onFeature(featureId);
                            break;
                        }
                    }
                }
            }
        }
    }
}
