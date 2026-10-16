package org.odk.collect.maplibre.staticmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
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
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_OSM
import org.odk.collect.shared.injection.Keys
import java.io.File
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.min
import kotlin.math.round
import kotlin.math.tan

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

    override fun load(
        configuration: StaticMapConfiguration,
        cache: StaticMapCache,
        trace: TraceDescription,
        width: Int,
        height: Int,
        callback: (Bitmap?) -> Unit
    ): () -> Unit {
        val cachedBitmap = cache.get(configuration, trace, width, height)
        if (cachedBitmap != null) {
            callback(cachedBitmap)
            return {}
        }

        val basemapConfiguration = when (configuration.basemapSource) {
            // Without Google tiles (no API key or no session) Google previews use OpenStreetMap instead
            BASEMAP_SOURCE_GOOGLE -> Configurations.all.getValue(BASEMAP_SOURCE_OSM)
            else -> Configurations.all[configuration.basemapSource]
        }

        if (basemapConfiguration == null || !MapLibreSupport.isAvailable() || trace.points.size < 2) {
            callback(null)
            return {}
        }

        MapLibreSupport.initialize(context)

        val fallbackStyle = basemapStyle(basemapConfiguration, configuration.basemapStyle, keys)

        val googleApiKey = keys.get("google")
        return if (configuration.basemapSource == BASEMAP_SOURCE_GOOGLE && !googleApiKey.isNullOrBlank()) {
            snapshotWithGoogleTiles(googleApiKey, fallbackStyle, configuration, cache, trace, width, height, callback)
        } else {
            snapshot(fallbackStyle, configuration, cache, trace, width, height, callback)
        }
    }

    private fun snapshotWithGoogleTiles(
        apiKey: String,
        fallbackStyle: Style.Builder,
        configuration: StaticMapConfiguration,
        cache: StaticMapCache,
        trace: TraceDescription,
        width: Int,
        height: Int,
        callback: (Bitmap?) -> Unit
    ): () -> Unit {
        var cancelled = false
        var cancelSnapshot: () -> Unit = {}

        GoogleMapTilesClient.getSession(apiKey, configuration.basemapStyle) { session ->
            if (cancelled) {
                return@getSession
            }

            if (session != null) {
                getGoogleCopyright(apiKey, session, trace, width, height) { copyright ->
                    if (!cancelled) {
                        cancelSnapshot = snapshot(googleBasemapStyle(apiKey, session, copyright), configuration, cache, trace, width, height, callback)
                    }
                }
            } else {
                // Not cached so that Google is tried again next time
                cancelSnapshot = snapshot(fallbackStyle, configuration, null, trace, width, height, callback)
            }
        }

        return {
            cancelled = true
            cancelSnapshot()
        }
    }

    private fun snapshot(
        style: Style.Builder,
        configuration: StaticMapConfiguration,
        cache: StaticMapCache?,
        trace: TraceDescription,
        width: Int,
        height: Int,
        callback: (Bitmap?) -> Unit
    ): () -> Unit {
        var snapshotter = MapSnapshotter(context, snapshotOptions(context, style, configuration.referenceLayer, trace, width, height))
        snapshotter.start(
            {
                cache?.put(configuration, trace, width, height, it.bitmap)
                callback(it.bitmap)
            },
            {
                // The basemap failed to load so retry without it
                snapshotter = MapSnapshotter(context, snapshotOptions(context, blankBasemap(), configuration.referenceLayer, trace, width, height))
                snapshotter.start(
                    { callback(it.bitmap) },
                    { callback(null) }
                )
            }
        )

        return { snapshotter.cancel() }
    }

    private fun snapshotOptions(
        context: Context,
        basemap: Style.Builder,
        referenceLayer: File?,
        trace: TraceDescription,
        width: Int,
        height: Int
    ): MapSnapshotter.Options {
        val horizontalPadding = (width * PADDING).toInt()
        val verticalPadding = (height * PADDING).toInt()

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
        val uri = basemapConfiguration.basemapUri(style)
        val uriValue = uri.value(keys)

        return when (uri) {
            is BasemapUri.Raster -> rasterBasemapStyle(uriValue, basemapConfiguration.attribution).fromUri("asset://maplibre_empty_style.json")
            is BasemapUri.Mapbox -> Style.Builder().fromUri(uriValue)
        }
    }

    private fun getGoogleCopyright(apiKey: String, session: String, trace: TraceDescription, width: Int, height: Int, callback: (String?) -> Unit) {
        val density = context.resources.displayMetrics.density
        val bounds = bounds(trace)

        // The zoom the snapshot ends up at, as the copyright depends on it
        val lonZoom = log2(360 * width / density * (1 - 2 * PADDING) / (256 * bounds.longitudeSpan))
        val latZoom = log2(2 * PI * height / density * (1 - 2 * PADDING) / (256 * (mercatorY(bounds.latitudeNorth) - mercatorY(bounds.latitudeSouth))))
        val zoom = round(min(lonZoom, latZoom)).coerceIn(0.0, 22.0).toInt()

        GoogleMapTilesClient.getCopyright(
            apiKey,
            session,
            zoom,
            bounds.latitudeNorth,
            bounds.latitudeSouth,
            bounds.longitudeEast,
            bounds.longitudeWest,
            callback
        )
    }

    private fun bounds(trace: TraceDescription): LatLngBounds {
        return LatLngBounds.Builder()
            .includes(trace.points.map { LatLng(it.latitude, it.longitude) })
            .build()
    }

    private fun mercatorY(latitude: Double): Double {
        return ln(tan(PI / 4 + Math.toRadians(latitude) / 2))
    }

    private fun googleBasemapStyle(apiKey: String, session: String, copyright: String?): Style.Builder {
        val attribution = listOfNotNull("Google Maps", copyright)
            .joinToString(" ") { "<a href=\"https://www.google.com/maps\">$it</a>" }

        return rasterBasemapStyle(GoogleMapTilesClient.getTilesUrl(apiKey, session), attribution)
            .fromUri("asset://maplibre_empty_style.json")
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
            builder.withSource(GeoJsonSource("trace_source", Polygon.fromLngLats(listOf(points))))
            builder.withLayer(
                FillLayer("trace_fill_layer", "trace_source").withProperties(
                    PropertyFactory.fillColor(trace.getFillColor())
                )
            )
        } else {
            builder.withSource(GeoJsonSource("trace_source", LineString.fromLngLats(points)))
        }

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

        // The margin left on each side of the trace, as a fraction of the map size
        const val PADDING = 0.1
    }
}
