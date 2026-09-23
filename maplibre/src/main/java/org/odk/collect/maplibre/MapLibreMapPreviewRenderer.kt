package org.odk.collect.maplibre

import android.content.Context
import android.graphics.Bitmap
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.android.snapshotter.MapSnapshotter
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.MapPreviewRenderer
import org.odk.collect.maps.traces.TraceDescription
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE

class MapLibreMapPreviewRenderer(
    private val context: Context,
    private val settingsProvider: SettingsProvider
) : MapPreviewRenderer {

    override fun render(
        trace: TraceDescription,
        width: Int,
        height: Int,
        callback: (Bitmap?) -> Unit
    ): () -> Unit {
        val basemapSource = settingsProvider.getUnprotectedSettings().getString(KEY_BASEMAP_SOURCE)
        val configuration = Configurations.all[basemapSource]

        if (configuration == null || trace.points.size < 2) {
            callback(null)
            return {}
        }

        MapLibre.getInstance(
            context,
            MapboxAccessToken.get(context),
            WellKnownTileServer.Mapbox
        )

        // Leave a 10% margin on each side
        val horizontalPadding = (width * 0.1).toInt()
        val verticalPadding = (height * 0.1).toInt()

        val density = context.resources.displayMetrics.density
        val options = MapSnapshotter.Options((width / density).toInt(), (height / density).toInt())
            .withPixelRatio(density)
            .withStyleBuilder(buildStyle(configuration, trace))
            .withLogo(false)
            .withRegion(
                LatLngBounds.Builder()
                    .includes(trace.points.map { LatLng(it.latitude, it.longitude) })
                    .build()
            )
            .withPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

        val snapshotter = MapSnapshotter(context, options)
        snapshotter.start(
            { callback(it.bitmap) },
            { callback(null) }
        )

        return { snapshotter.cancel() }
    }

    private fun buildStyle(configuration: Configuration, trace: TraceDescription): Style.Builder {
        val builder = basemap(configuration)
        addTrace(builder, trace)
        return builder
    }

    private fun basemap(configuration: Configuration): Style.Builder {
        return when (val uri = configuration.basemapUri(settingsProvider.getUnprotectedSettings())) {
            is BasemapUri.Raster -> {
                val tileSet = TileSet("2.1.0", uri.value).apply {
                    attribution = configuration.attribution ?: ""
                    scheme = "xyz"
                }

                Style.Builder()
                    .withSource(RasterSource("basemap_source", tileSet))
                    .withLayer(RasterLayer("basemap_layer", "basemap_source"))
            }

            is BasemapUri.Mapbox -> Style.Builder().fromUri(uri.value)
        }
    }

    private fun addTrace(builder: Style.Builder, trace: TraceDescription) {
        builder.withSource(GeoJsonSource("trace_source", geoJson(trace.points)))
        builder.withLayer(
            LineLayer("trace_layer", "trace_source").withProperties(
                PropertyFactory.lineColor(trace.getStrokeColor()),
                PropertyFactory.lineWidth(MapUtils.convertStrokeWidth(trace)),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
            )
        )
    }

    private fun geoJson(points: List<MapPoint>): String {
        val coordinates = JSONArray().apply {
            points.forEach {
                put(JSONArray().put(it.longitude).put(it.latitude))
            }
        }

        return JSONObject()
            .put("type", "LineString")
            .put("coordinates", coordinates)
            .toString()
    }
}
