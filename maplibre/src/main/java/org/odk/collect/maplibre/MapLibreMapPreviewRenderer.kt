package org.odk.collect.maplibre

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.android.snapshotter.MapSnapshotter
import org.maplibre.android.style.layers.BackgroundLayer
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
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_GOOGLE
import org.odk.collect.settings.keys.ProjectKeys.BASEMAP_SOURCE_OSM
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
        val configuration = when (basemapSource) {
            // Google Maps can't render off-screen, so its previews use OpenStreetMap instead
            BASEMAP_SOURCE_GOOGLE -> Configurations.all.getValue(BASEMAP_SOURCE_OSM)
            else -> Configurations.all[basemapSource]
        }

        if (configuration == null || !MapLibreSupport.isAvailable() || trace.points.size < 2) {
            callback(null)
            return {}
        }

        MapLibreSupport.initialize(context)

        var snapshotter = MapSnapshotter(context, snapshotOptions(basemap(configuration), trace, width, height))
        snapshotter.start(
            { callback(it.bitmap) },
            {
                // The basemap failed to load so retry without it
                snapshotter = MapSnapshotter(context, snapshotOptions(blankBasemap(), trace, width, height))
                snapshotter.start(
                    { callback(it.bitmap) },
                    { callback(null) }
                )
            }
        )

        return { snapshotter.cancel() }
    }

    private fun snapshotOptions(
        basemap: Style.Builder,
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
            .withStyleBuilder(buildStyle(basemap, trace))
            .withLogo(false)
            .withRegion(
                LatLngBounds.Builder()
                    .includes(trace.points.map { LatLng(it.latitude, it.longitude) })
                    .build()
            )
            .withPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
    }

    private fun buildStyle(basemap: Style.Builder, trace: TraceDescription): Style.Builder {
        addReferenceLayer(basemap)
        addTrace(basemap, trace)
        return basemap
    }

    private fun basemap(configuration: Configuration): Style.Builder {
        return when (val uri = configuration.basemapUri(settingsProvider.getUnprotectedSettings())) {
            is BasemapUri.Raster -> configuration.rasterBasemapStyle(uri).fromUri("asset://maplibre_empty_style.json")
            is BasemapUri.Mapbox -> Style.Builder().fromUri(uri.value)
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
