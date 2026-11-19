package org.odk.collect.googlemaps

import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import org.odk.collect.maps.layers.MbtilesFile
import java.io.Closeable

class GoogleMapsOfflineTileProvider(private val mbtiles: MbtilesFile) : TileProvider, Closeable {
    private val minimumZoom = readZoom("minzoom") ?: Int.MIN_VALUE

    private val maximumZoom = readZoom("maxzoom") ?: Int.MAX_VALUE

    override fun getTile(x: Int, y: Int, z: Int): Tile {
        if (z !in minimumZoom..maximumZoom) {
            return TileProvider.NO_TILE
        }

        val data = mbtiles.getTileBlob(z, x, y) ?: return TileProvider.NO_TILE
        return Tile(256, 256, data)
    }

    override fun close() {
        mbtiles.close()
    }

    private fun readZoom(name: String): Int? {
        return try {
            mbtiles.getMetadata(name).trim().toDoubleOrNull()?.toInt()
        } catch (_: MbtilesFile.MbtilesException) {
            null
        }
    }
}
