package org.odk.collect.maplibre

import android.graphics.Color
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.Source
import org.maplibre.android.style.sources.TileSet
import org.maplibre.android.style.sources.VectorSource
import org.odk.collect.maps.layers.MbtilesFile
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * MapLibre only knows how to fetch tiles via HTTP, so to display tiles from a reference layer file
 * they have to be served locally over HTTP.
 */
internal class ReferenceLayers {

    private val tileServer = lazy {
        try {
            TileHttpServer().also { it.start() }
        } catch (_: IOException) {
            null
        }
    }
    private var referenceLayer: Pair<File, MbtilesFile>? = null

    fun sourceAndLayers(file: File): Pair<Source, List<Layer>>? {
        val server = tileServer.value ?: return null

        val mbtiles = referenceLayer?.takeIf { it.first == file }?.second ?: try {
            MbtilesFile(file).also {
                referenceLayer?.second?.close()
                referenceLayer = file to it
            }
        } catch (_: MbtilesFile.MbtilesException) {
            return null
        }

        server.addSource(file.name, mbtiles)
        Timber.i("Added %s as a %s layer at /%s", file, mbtiles.layerType, file.name)

        return mbtilesSourceAndLayers(file.name, mbtiles, server.getUrlTemplate(file.name))
    }

    fun destroy() {
        if (tileServer.isInitialized()) {
            tileServer.value?.destroy()
        }
    }

    private fun mbtilesSourceAndLayers(
        id: String,
        mbtiles: MbtilesFile,
        urlTemplate: String
    ): Pair<Source, List<Layer>> {
        val tileSet = createTileSet(mbtiles, urlTemplate)

        return if (mbtiles.layerType == MbtilesFile.LayerType.VECTOR) {
            VectorSource(id, tileSet) to mbtiles.vectorLayers.map { layer ->
                // Pick a colour that's a function of the filename and layer name.
                // The colour will appear essentially random; the only purpose here
                // is to try to assign different colours to different layers, such
                // that each individual layer appears in its own consistent colour.
                val hue = ((id + "." + layer.name).hashCode() and 0x7fffffff) % 360
                LineLayer(id + "." + layer.name, id)
                    .withSourceLayer(layer.name)
                    .withProperties(
                        PropertyFactory.lineColor(
                            Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.7f, 1f))
                        ),
                        PropertyFactory.lineWidth(1.0f),
                        PropertyFactory.lineOpacity(0.7f)
                    )
            }
        } else {
            RasterSource(id, tileSet) to listOf(RasterLayer("$id.raster", id))
        }
    }

    private fun createTileSet(mbtiles: MbtilesFile, urlTemplate: String): TileSet {
        val tileSet = TileSet("2.2.0", urlTemplate)

        // Configure the TileSet using the metadata in the .mbtiles file.
        try {
            tileSet.name = mbtiles.getMetadata("name")
            try {
                tileSet.minZoom = mbtiles.getMetadata("minzoom").toFloat()
                tileSet.maxZoom = mbtiles.getMetadata("maxzoom").toFloat()
            } catch (_: NumberFormatException) {
                // ignore
            }
            var parts = mbtiles.getMetadata("center").split(",").toTypedArray()
            if (parts.size == 3) { // latitude, longitude, zoom
                try {
                    tileSet.setCenter(
                        parts[0].toFloat(),
                        parts[1].toFloat(),
                        parts[2].toFloat()
                    )
                } catch (e: NumberFormatException) {
                    // ignore
                }
            }
            parts = mbtiles.getMetadata("bounds").split(",").toTypedArray()
            if (parts.size == 4) { // left, bottom, right, top
                try {
                    tileSet.setBounds(
                        parts[0].toFloat(),
                        parts[1].toFloat(),
                        parts[2].toFloat(),
                        parts[3].toFloat()
                    )
                } catch (_: NumberFormatException) {
                    // ignore
                }
            }
        } catch (e: MbtilesFile.MbtilesException) {
            Timber.w(e.message)
        }
        return tileSet
    }
}
