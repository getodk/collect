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

    private fun getOverzoomedTile(x: Int, y: Int, z: Int): Tile {
        // Each zoom level splits a tile into 2x2 smaller ones, so a tile at max zoom (the parent)
        // is split into tilesPerSide x tilesPerSide tiles at zoom z (e.g. 4x4 two levels deeper).
        // x / tilesPerSide and y / tilesPerSide give the parent.
        val tilesPerSide = 2.0.pow(z - maximumZoom).toInt()
        val parentData = mbtiles.getTileBlob(maximumZoom, x / tilesPerSide, y / tilesPerSide)
            ?: return TileProvider.NO_TILE
        val parent = BitmapFactory.decodeByteArray(parentData, 0, parentData.size)
            ?: return TileProvider.NO_TILE

        // x % tilesPerSide and y % tilesPerSide give the position of this tile inside the parent.
        // Stretch the parent to tilesPerSide times its size and shift it left and up by that many
        // tiles so that only this tile's part lands on the bitmap.
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
