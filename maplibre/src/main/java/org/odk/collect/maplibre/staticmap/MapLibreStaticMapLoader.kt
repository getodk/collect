package org.odk.collect.maplibre.staticmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.android.snapshotter.MapSnapshotter
import org.maplibre.android.style.layers.BackgroundLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.odk.collect.maplibre.BasemapUri
import org.odk.collect.maplibre.Configuration
import org.odk.collect.maplibre.Configurations
import org.odk.collect.maplibre.MapLibreDependencyComponentProvider
import org.odk.collect.maplibre.MapLibreSupport
import org.odk.collect.maplibre.MapUtils
import org.odk.collect.maplibre.ReferenceLayersServer
import org.odk.collect.maplibre.basemapUri
import org.odk.collect.maplibre.rasterBasemapStyle
import org.odk.collect.maps.traces.TraceDescription
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_GOOGLE
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_OSM
import org.odk.collect.shared.injection.Keys
import java.io.File
import javax.inject.Inject

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
            // Google Maps can't render off-screen, so its previews use OpenStreetMap instead
            BASEMAP_SOURCE_GOOGLE -> Configurations.all.getValue(BASEMAP_SOURCE_OSM)
            else -> Configurations.all[configuration.basemapSource]
        }

        if (basemapConfiguration == null || !MapLibreSupport.isAvailable() || trace.points.size < 2) {
            callback(null)
            return {}
        }

        MapLibreSupport.initialize(context)

        val style = basemapStyle(basemapConfiguration, configuration.basemapStyle, keys)

        var snapshotter = MapSnapshotter(context, snapshotOptions(context, style, configuration.referenceLayer, trace, width, height))
        snapshotter.start(
            {
                cache.put(configuration, trace, width, height, it.bitmap)
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
        // Leave a 10% margin on each side
        val horizontalPadding = (width * 0.1).toInt()
        val verticalPadding = (height * 0.1).toInt()

        val density = context.resources.displayMetrics.density
        return MapSnapshotter.Options((width / density).toInt(), (height / density).toInt())
            .withPixelRatio(density)
            .withStyleBuilder(buildStyle(basemap, referenceLayer, trace))
            .withLogo(false)
            .withRegion(
                LatLngBounds.Builder()
                    .includes(trace.points.map { LatLng(it.latitude, it.longitude) })
                    .build()
            )
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
            is BasemapUri.Raster -> basemapConfiguration.rasterBasemapStyle(uriValue).fromUri("asset://maplibre_empty_style.json")
            is BasemapUri.Mapbox -> Style.Builder().fromUri(uriValue)
        }
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
        builder.withSource(GeoJsonSource("trace_source", LineString.fromLngLats(trace.points.map { Point.fromLngLat(it.longitude, it.latitude) })))
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
    }
}
