package org.odk.collect.maplibre.staticmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.suspendCancellableCoroutine
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.android.snapshotter.MapSnapshotter
import org.maplibre.android.style.layers.BackgroundLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import org.odk.collect.maplibre.BasemapUri
import org.odk.collect.maplibre.Configuration
import org.odk.collect.maplibre.Configurations
import org.odk.collect.maplibre.MapLibreDependencyComponentProvider
import org.odk.collect.maplibre.MapLibreSupport
import org.odk.collect.maplibre.MapUtils
import org.odk.collect.maplibre.ReferenceLayersServer
import org.odk.collect.maplibre.basemapUri
import org.odk.collect.maplibre.rasterBasemapStyle
import org.odk.collect.maps.traces.PolygonDescription
import org.odk.collect.maps.traces.TraceDescription
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_GOOGLE
import org.odk.collect.shared.injection.Keys
import org.odk.collect.strings.R
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.math.log2
import kotlin.math.max

/**
 * Loads a static image of a map showing a line or a polygon, rendering it off-screen if it isn't cached.
 */
class MapLibreStaticMapLoader(context: Context) : StaticMapLoader {

    @Inject
    lateinit var keys: Keys

    private val context = context.applicationContext

    init {
        (this.context as MapLibreDependencyComponentProvider)
            .mapLibreDependencyComponent
            .inject(this)
    }

    override suspend fun load(
        configuration: StaticMapConfiguration,
        cache: StaticMapCache,
        trace: TraceDescription,
        width: Int,
        height: Int
    ): Bitmap? {
        val cachedBitmap = cache.get(configuration, trace, width, height)
        if (cachedBitmap != null) {
            return cachedBitmap
        }

        if (!MapLibreSupport.isAvailable() || trace.points.size < 2) {
            return null
        }

        MapLibreSupport.initialize(context)

        return if (configuration.basemapSource == BASEMAP_SOURCE_GOOGLE) {
            snapshotWithGoogleTiles(keys.get("google").orEmpty(), configuration, cache, trace, width, height)
        } else {
            val basemapConfiguration = Configurations.all.getValue(configuration.basemapSource)
            snapshot(basemapStyle(basemapConfiguration, configuration.basemapStyle, keys), configuration, cache, trace, width, height)
        }
    }

    private suspend fun snapshotWithGoogleTiles(
        apiKey: String,
        configuration: StaticMapConfiguration,
        cache: StaticMapCache,
        trace: TraceDescription,
        width: Int,
        height: Int
    ): Bitmap? {
        val session = GoogleMapTilesClient.getSession(apiKey, configuration.basemapStyle)

        return if (session == null) {
            // Not cached so that Google is tried again next time
            blankSnapshot(configuration, trace, width, height)
        } else {
            val bounds = bounds(trace)
            val copyright = GoogleMapTilesClient.getCopyright(apiKey, session, estimateZoom(bounds, width), bounds)
            // Not cached without the copyright so that it's requested again next time
            val snapshotCache = if (copyright != null) cache else null
            snapshot(googleBasemapStyle(apiKey, session, copyright), configuration, snapshotCache, trace, width, height)
        }
    }

    private suspend fun snapshot(
        style: Style.Builder,
        configuration: StaticMapConfiguration,
        cache: StaticMapCache?,
        trace: TraceDescription,
        width: Int,
        height: Int
    ): Bitmap? {
        val bitmap = takeSnapshot(snapshotOptions(context, style, configuration.referenceLayer, trace, width, height))
        if (bitmap != null) {
            cache?.put(configuration, trace, width, height, bitmap)
            return bitmap
        }

        // The basemap failed to load so retry without it
        return blankSnapshot(configuration, trace, width, height)
    }

    private suspend fun blankSnapshot(configuration: StaticMapConfiguration, trace: TraceDescription, width: Int, height: Int): Bitmap? {
        return takeSnapshot(snapshotOptions(context, blankBasemap(), configuration.referenceLayer, trace, width, height))
    }

    private suspend fun takeSnapshot(options: MapSnapshotter.Options): Bitmap? {
        return suspendCancellableCoroutine { continuation ->
            val snapshotter = MapSnapshotter(context, options)
            snapshotter.start({ continuation.resume(it.bitmap) }, { continuation.resume(null) })
            continuation.invokeOnCancellation { snapshotter.cancel() }
        }
    }

    private fun snapshotOptions(
        context: Context,
        basemap: Style.Builder,
        referenceLayer: File?,
        trace: TraceDescription,
        width: Int,
        height: Int
    ): MapSnapshotter.Options {
        // Leave a 10% margin on each side
        val horizontalPadding = (width * 0.1).toInt()
        val verticalPadding = (height * 0.1).toInt()

        val density = context.resources.displayMetrics.density
        return MapSnapshotter.Options((width / density).toInt(), (height / density).toInt())
            .withPixelRatio(density)
            .withStyleBuilder(buildStyle(basemap, referenceLayer, trace))
            .withLogo(false)
            .withRegion(bounds(trace))
            .withPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
    }

    private fun buildStyle(basemap: Style.Builder, referenceLayer: File?, trace: TraceDescription): Style.Builder {
        addReferenceLayer(basemap, referenceLayer)
        addTrace(basemap, trace)
        return basemap
    }

    private fun basemapStyle(basemapConfiguration: Configuration, style: String?, keys: Keys): Style.Builder {
        return when (val uri = basemapConfiguration.basemapUri(style)) {
            is BasemapUri.Raster -> basemapConfiguration.rasterBasemapStyle(style, keys).fromUri("asset://maplibre_empty_style.json")
            is BasemapUri.Mapbox -> Style.Builder().fromUri(uri.value(keys))
        }
    }

    /**
     * Roughly the zoom at which the line or polygon fills the map, which is close enough for the
     * copyright.
     */
    private fun estimateZoom(bounds: LatLngBounds, width: Int): Int {
        val mapWidth = width / context.resources.displayMetrics.density.toDouble()
        val traceSpan = max(bounds.longitudeSpan, bounds.latitudeSpan)

        // At zoom 0 the whole world (360°) fits in one tile and every zoom level doubles its size
        val zoomToFillMap = log2(mapWidth / TILE_SIZE * 360 / traceSpan)

        return zoomToFillMap.toInt().coerceIn(0, GOOGLE_MAX_ZOOM)
    }

    private fun bounds(trace: TraceDescription): LatLngBounds {
        return LatLngBounds.Builder()
            .includes(trace.points.map { LatLng(it.latitude, it.longitude) })
            .build()
    }

    private fun googleBasemapStyle(apiKey: String, session: String, copyright: String?): Style.Builder {
        val attribution = listOfNotNull("Google Maps", copyright)
            .joinToString(" ") { "<a href=\"https://www.google.com/maps\">$it</a>" }

        return Configuration(
            name = R.string.basemap_source_google,
            uri = BasemapUri.Raster(GoogleMapTilesClient.getTilesUrl(apiKey, session)),
            attribution = attribution
        ).rasterBasemapStyle(null, keys).fromUri("asset://maplibre_empty_style.json")
    }

    private fun blankBasemap(): Style.Builder {
        return Style.Builder()
            .fromUri("asset://maplibre_empty_style.json")
            .withLayer(
                BackgroundLayer("background_layer").withProperties(
                    PropertyFactory.backgroundColor(Color.rgb(224, 224, 224))
                )
            )
    }

    private fun addReferenceLayer(builder: Style.Builder, file: File?) {
        if (file == null) {
            return
        }

        val (source, layers) = referenceLayersServer.sourceAndLayers(file) ?: return
        builder.withSource(source)
        layers.forEach { builder.withLayer(it) }
    }

    private fun addTrace(builder: Style.Builder, trace: TraceDescription) {
        val points = trace.points.map { Point.fromLngLat(it.longitude, it.latitude) }

        if (trace is PolygonDescription) {
            builder.withSource(GeoJsonSource("trace_fill_source", Polygon.fromLngLats(listOf(points))))
            builder.withLayer(
                FillLayer("trace_fill_layer", "trace_fill_source").withProperties(
                    PropertyFactory.fillColor(trace.getFillColor())
                )
            )
        }

        // A polygon's outline is drawn as a line because MapLibre doesn't render polygons with (almost) no area
        builder.withSource(GeoJsonSource("trace_source", LineString.fromLngLats(points)))
        builder.withLayer(
            LineLayer("trace_layer", "trace_source").withProperties(
                PropertyFactory.lineColor(trace.getStrokeColor()),
                PropertyFactory.lineWidth(MapUtils.convertStrokeWidth(trace)),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
            )
        )
    }

    private companion object {
        // Shared by all loaders so that widgets, which are recreated often, don't each start a server
        val referenceLayersServer = ReferenceLayersServer()

        // The Map Tiles API only accepts zoom levels up to this one
        const val GOOGLE_MAX_ZOOM = 22

        // Width of a map tile in dp
        const val TILE_SIZE = 256
    }
}
