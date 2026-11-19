package org.odk.collect.googlemaps

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import org.odk.collect.maps.layers.MbtilesFile
import java.io.ByteArrayOutputStream
import java.io.Closeable
import kotlin.math.pow

class GoogleMapsOfflineTileProvider(private val mbtiles: MbtilesFile) : TileProvider, Closeable {
    private val minimumZoom = mbtiles.minZoom ?: Int.MIN_VALUE

    private val maximumZoom = mbtiles.maxZoom ?: Int.MAX_VALUE

    override fun getTile(x: Int, y: Int, z: Int): Tile = when {
        z < minimumZoom -> TileProvider.NO_TILE
        z > maximumZoom -> getOverzoomedTile(x, y, z)
        else -> mbtiles.getTileBlob(z, x, y)?.let { Tile(256, 256, it) } ?: TileProvider.NO_TILE
    }

    override fun close() {
        mbtiles.close()
    }

    /**
     * Google Maps doesn't scale up tiles beyond the layer's max zoom like MapLibre does, so
     * build them by scaling up the matching part of the parent tile at the max zoom.
     */
    private fun getOverzoomedTile(x: Int, y: Int, z: Int): Tile {
        val tilesPerSide = 2.0.pow(z - maximumZoom).toInt()
        val parentData = mbtiles.getTileBlob(maximumZoom, x / tilesPerSide, y / tilesPerSide)
            ?: return TileProvider.NO_TILE
        val parent = BitmapFactory.decodeByteArray(parentData, 0, parentData.size)
            ?: return TileProvider.NO_TILE

        val child = createBitmap(parent.width, parent.height).applyCanvas {
            val left = -(x % tilesPerSide) * parent.width.toFloat()
            val top = -(y % tilesPerSide) * parent.height.toFloat()
            val right = left + tilesPerSide * parent.width.toFloat()
            val bottom = top + tilesPerSide * parent.height.toFloat()
            drawBitmap(parent, null, RectF(left, top, right, bottom), Paint(Paint.FILTER_BITMAP_FLAG))
        }

        val data = ByteArrayOutputStream().also {
            child.compress(Bitmap.CompressFormat.PNG, 100, it)
        }.toByteArray()
        return Tile(parent.width, parent.height, data)
    }
}
