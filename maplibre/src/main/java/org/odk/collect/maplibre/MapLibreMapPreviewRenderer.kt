package org.odk.collect.maplibre

import android.content.Context
import android.graphics.Bitmap
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.android.snapshotter.MapSnapshotter
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.odk.collect.maps.MapPoint
import org.odk.collect.maps.MapPreviewRenderer
import org.odk.collect.maps.layers.MapFragmentReferenceLayerUtils
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.maps.traces.TraceDescription
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys.KEY_BASEMAP_SOURCE
import org.odk.collect.settings.keys.ProjectKeys.KEY_REFERENCE_LAYER
import javax.inject.Provider

class MapLibreMapPreviewRenderer(
    private val context: Context,
    private val settingsProvider: SettingsProvider,
    private val referenceLayerRepository: Provider<ReferenceLayerRepository>
) : MapPreviewRenderer {

    private val referenceLayers = ReferenceLayers()

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

        MapLibreSupport.initialize(context)

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
        addReferenceLayer(builder)
        addTrace(builder, trace)
        return builder
    }

    private fun basemap(configuration: Configuration): Style.Builder {
        return when (val uri = configuration.basemapUri(settingsProvider.getUnprotectedSettings())) {
            is BasemapUri.Raster -> configuration.rasterBasemapStyle(uri)
            is BasemapUri.Mapbox -> Style.Builder().fromUri(uri.value)
        }
    }

    private fun addReferenceLayer(builder: Style.Builder) {
        val file = MapFragmentReferenceLayerUtils.getReferenceLayerFile(
            settingsProvider.getUnprotectedSettings().getString(KEY_REFERENCE_LAYER),
            referenceLayerRepository.get()
        ) ?: return

        val (source, layers) = referenceLayers.sourceAndLayers(file) ?: return
        builder.withSource(source)
        layers.forEach { builder.withLayer(it) }
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
