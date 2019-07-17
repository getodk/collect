package org.odk.collect.android.map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.view.Gravity;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
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
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.layers.TransitionOptions;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.map.MbtilesFile.LayerType;
import org.odk.collect.android.map.MbtilesFile.MbtilesException;
import org.odk.collect.android.mapboxsdk.MapFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import timber.log.Timber;

import static android.os.Looper.getMainLooper;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.rasterOpacity;

public class MapboxMapFragment extends MapFragment implements org.odk.collect.android.map.MapFragment,
    OnMapReadyCallback, MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener,
    LocationEngineCallback<LocationEngineResult> {

    public static final LatLng INITIAL_CENTER = new LatLng(0, -30);
    public static final float INITIAL_ZOOM = 2;
    public static final float POINT_ZOOM = 16;
    public static final String POINT_ICON_ID = "point-icon-id";
    public static final long LOCATION_INTERVAL_MILLIS = 1000;
    public static final long LOCATION_MAX_WAIT_MILLIS = 5*LOCATION_INTERVAL_MILLIS;

    protected MapboxMap map;
    protected List<ReadyListener> gpsLocationReadyListeners = new ArrayList<>();
    protected PointListener gpsLocationListener;
    protected PointListener clickListener;
    protected PointListener longPressListener;
    protected FeatureListener dragEndListener;

    protected LocationComponent locationComponent;
    protected boolean locationEnabled;
    protected MapPoint lastLocationFix;

    protected int nextFeatureId = 1;
    protected Map<Integer, MapFeature> features = new HashMap<>();
    protected SymbolManager symbolManager;
    protected LineManager lineManager;
    protected boolean isDragging;
    protected final String styleUrl;
    protected File referenceLayerFile;
    protected List<Layer> overlayLayers = new ArrayList<>();
    protected List<Source> overlaySources = new ArrayList<>();

    protected TileHttpServer tileServer;

    // During Robolectric tests, Google Play Services is unavailable; sadly, the
    // "map" field will be null and many operations will need to be stubbed out.
    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "This flag is exposed for Robolectric tests to set")
    @VisibleForTesting public static boolean testMode;

    public MapboxMapFragment(String styleUrl) {
        this.styleUrl = styleUrl;
    }

    @Override public void addTo(
        @NonNull FragmentActivity activity, int containerId,
        @Nullable ReadyListener readyListener, @Nullable ErrorListener errorListener) {
        Context context = getContext();
        if (MapboxUtils.initMapbox() == null) {
            MapConfigurator.getCurrent(context).source.showUnavailableMessage(context);
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

        activity.getSupportFragmentManager()
            .beginTransaction().replace(containerId, this).commitNow();
        getMapAsync(map -> {
            this.map = map;  // signature of getMapAsync() ensures map is never null

            map.setStyle(getDesiredStyleBuilder(), style -> {
                map.getUiSettings().setCompassGravity(Gravity.TOP | Gravity.START);
                map.getUiSettings().setCompassMargins(36, 36, 36, 36);
                map.getStyle().setTransition(new TransitionOptions(0, 0, false));

                Drawable pointIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_map_point);
                map.getStyle().addImage(POINT_ICON_ID, pointIcon);

                map.addOnMapClickListener(this);
                map.addOnMapLongClickListener(this);

                // MAPBOX ISSUE: Only the last-created manager gets draggable annotations.
                // https://github.com/mapbox/mapbox-plugins-android/issues/863
                // For symbols to be draggable, SymbolManager must be created last.
                lineManager = createLineManager();
                symbolManager = createSymbolManager();

                loadReferenceOverlay();
                enableLocationComponent();

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(INITIAL_CENTER, INITIAL_ZOOM));
                if (readyListener != null) {
                    readyListener.onReady(this);
                }
            });

            // In Robolectric tests, getMapAsync() never gets around to calling its
            // callback; we have to invoke the ready listener directly.
            if (testMode && readyListener != null) {
                readyListener.onReady(this);
            }
        });
    }

    @Override public void onDestroy() {
        if (tileServer != null) {
            tileServer.destroy();
        }
        super.onDestroy();
    }

    @Override public Fragment getFragment() {
        return this;
    }

    private Style.Builder getDesiredStyleBuilder() {
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

    @Override public void setReferenceLayerFile(File file) {
        referenceLayerFile = file;
        if (map != null) {
            loadReferenceOverlay();
        }
    }

    /** Updates the map to reflect the value of referenceLayerFile. */
    private void loadReferenceOverlay() {
        clearOverlays();
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
                int hue = (((id + "." + layer.name).hashCode()) & 0x7fffffff) % 360;
                addOverlayLayer(new FillLayer(id + "/" + layer.name + ".fill", id).withProperties(
                    fillColor(Color.HSVToColor(new float[] {hue, 0.3f, 1})),
                    fillOpacity(0.1f)
                ).withSourceLayer(layer.name));
                addOverlayLayer(new LineLayer(id + "/" + layer.name + ".line", id).withProperties(
                    lineColor(Color.HSVToColor(new float[] {hue, 0.7f, 1})),
                    lineWidth(1f),
                    lineOpacity(0.7f)
                ).withSourceLayer(layer.name));
            }
        }
        if (mbtiles.getLayerType() == LayerType.RASTER) {
            addOverlaySource(new RasterSource(id, tileSet));
            addOverlayLayer(new RasterLayer(id + ".raster", id).withProperties(
                rasterOpacity(0.5f)
            ));
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

    private void clearOverlays() {
        Style style = map.getStyle();
        for (Layer layer : overlayLayers) {
            style.removeLayer(layer);
        }
        overlayLayers.clear();
        for (Source source : overlaySources) {
            style.removeSource(source);
        }
        overlaySources.clear();
    }

    private void addOverlayLayer(Layer layer) {
        map.getStyle().addLayer(layer);
        overlayLayers.add(layer);
    }

    private void addOverlaySource(Source source) {
        map.getStyle().addSource(source);
        overlaySources.add(source);
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

    @SuppressWarnings({"MissingPermission"})  // Permission checks for location services handled in widgets
    private void enableLocationComponent() {
        if (map == null) {  // map is null during Robolectric tests
            return;
        }

        LocationEngineRequest request = new LocationEngineRequest.Builder(LOCATION_INTERVAL_MILLIS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(LOCATION_MAX_WAIT_MILLIS)
            .build();

        LocationEngine engine = LocationEngineProvider.getBestLocationEngine(getContext());
        engine.requestLocationUpdates(request, this, getMainLooper());
        engine.getLastLocation(this);

        locationComponent = map.getLocationComponent();
        if (map.getStyle() != null) {
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(getContext(), map.getStyle())
                    .locationEngine(engine)
                    .locationComponentOptions(
                        LocationComponentOptions.builder(getContext())
                            .foregroundDrawable(R.drawable.ic_crosshairs)
                            .backgroundDrawable(R.drawable.empty)
                            .enableStaleState(false)
                            .elevation(0)  // removes the shadow
                            .build()
                    )
                    .build()
            );
        }

        locationComponent.setCameraMode(CameraMode.NONE);
        locationComponent.setRenderMode(RenderMode.NORMAL);
        locationComponent.setLocationComponentEnabled(locationEnabled);
    }

    @Override public @NonNull MapPoint getCenter() {
        if (map == null) {  // during Robolectric tests, map will be null
            return fromLatLng(INITIAL_CENTER);
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

    protected void moveOrAnimateCamera(CameraUpdate movement, boolean animate) {
        if (animate) {
            map.animateCamera(movement);
        } else {
            map.moveCamera(movement);
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

    @Override public int addMarker(MapPoint point, boolean draggable) {
        int featureId = nextFeatureId++;
        features.put(featureId, new MarkerFeature(featureId, symbolManager, point, draggable));
        return featureId;
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

    @Override public void setDragEndListener(@Nullable FeatureListener listener) {
        dragEndListener = listener;
    }

    @Override public @Nullable String getLocationProvider() {
        return null;
    }

    @Override public boolean onMapClick(@NonNull LatLng point) {
        // MAPBOX ISSUE: Dragging can also generate map click events, and the
        // Mapbox SDK seems to provide no way to prevent drag touches from being
        // passed through to the map and being interpreted as a map click.
        // Our workaround is to track drags in progress with the isDragging flag.
        if (clickListener != null && !isDragging) {
            clickListener.onPoint(fromLatLng(point));
        }
        return true;
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

    protected LatLngBounds expandBounds(LatLngBounds bounds, double factor) {
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

    @Override public void setGpsLocationEnabled(boolean enable) {
        locationEnabled = enable;
        if (locationComponent != null) {
            locationComponent.setLocationComponentEnabled(enable);
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

    @Override public void onSuccess(LocationEngineResult result) {
        lastLocationFix = fromLocation(result.getLastLocation());
        if (locationComponent != null) {
            locationComponent.forceLocationUpdate(result.getLastLocation());
        }
        for (ReadyListener listener : gpsLocationReadyListeners) {
            listener.onReady(this);
        }
        gpsLocationReadyListeners.clear();
        if (gpsLocationListener != null) {
            gpsLocationListener.onPoint(lastLocationFix);
        }
    }

    @Override public void onFailure(@NonNull Exception exception) { }

    protected static @NonNull MapPoint fromLatLng(@NonNull LatLng latLng) {
        return new MapPoint(latLng.getLatitude(), latLng.getLongitude());
    }

    protected static @Nullable MapPoint fromLocation(@Nullable Location location) {
        if (location == null) {
            return null;
        }
        return new MapPoint(location.getLatitude(), location.getLongitude(),
            location.getAltitude(), location.getAccuracy());
    }

    protected static @NonNull MapPoint fromSymbol(@NonNull Symbol symbol, double alt, double sd) {
        LatLng position = symbol.getLatLng();
        return new MapPoint(position.getLatitude(), position.getLongitude(), alt, sd);
    }

    protected static @NonNull LatLng toLatLng(@NonNull MapPoint point) {
        return new LatLng(point.lat, point.lon);
    }

    protected Symbol createSymbol(SymbolManager symbolManager, MapPoint point, boolean draggable) {
        return symbolManager.create(new SymbolOptions()
            .withLatLng(toLatLng(point))
            .withIconImage(POINT_ICON_ID)
            .withIconSize(1f)
            .withZIndex(10)
            .withDraggable(draggable)
            .withTextOpacity(0f)
        );
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
    protected class MarkerFeature implements MapFeature {
        private final int featureId;
        private final SymbolManager symbolManager;
        private final DragListener dragListener = new DragListener();
        private MapPoint point;
        private Symbol symbol;

        public MarkerFeature(int featureId, SymbolManager symbolManager, MapPoint point, boolean draggable) {
            this.featureId = featureId;
            this.symbolManager = symbolManager;
            this.point = point;
            this.symbol = createSymbol(symbolManager, point, draggable);
            symbolManager.addDragListener(dragListener);
        }

        public MapPoint getPoint() {
            return point;
        }

        public void dispose() {
            symbolManager.removeDragListener(dragListener);
            symbolManager.delete(symbol);
            symbol = null;
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
    protected class PolyFeature implements MapFeature {
        public static final float STROKE_WIDTH = 5;

        private final int featureId;
        private final LineManager lineManager;
        private final SymbolManager symbolManager;
        private final DragListener dragListener = new DragListener();
        private final List<MapPoint> points = new ArrayList<>();
        private final List<Symbol> symbols = new ArrayList<>();
        private final boolean closedPolygon;
        private Line line;

        public PolyFeature(int featureId, LineManager lineManager, SymbolManager symbolManager,
            Iterable<MapPoint> points, boolean closedPolygon) {
            this.featureId = featureId;
            this.lineManager = lineManager;
            this.symbolManager = symbolManager;
            this.closedPolygon = closedPolygon;
            for (MapPoint point : points) {
                this.points.add(point);
                this.symbols.add(createSymbol(symbolManager, point, true));
            }
            line = lineManager.create(new LineOptions()
                .withLineColor(ColorUtils.colorToRgbaString(getResources().getColor(R.color.mapLine)))
                .withLineWidth(STROKE_WIDTH)
                .withLatLngs(new ArrayList<>())
            );
            updateLine();
            symbolManager.addDragListener(dragListener);
        }

        public List<MapPoint> getPoints() {
            return new ArrayList<>(points);
        }

        public void dispose() {
            if (line != null) {
                lineManager.delete(line);
                line = null;
            }
            symbolManager.removeDragListener(dragListener);
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
            symbols.add(createSymbol(symbolManager, point, true));
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

        protected void updateLine() {
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

        class DragListener implements OnSymbolDragListener {
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
