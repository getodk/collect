package org.odk.collect.googlemaps

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import java.io.Closeable
import java.io.File

class GoogleMapsOfflineTileProvider(file: File) : TileProvider, Closeable {
    var minimumZoom = Int.MIN_VALUE
        private set

    var maximumZoom = Int.MAX_VALUE
        private set

    var bounds: LatLngBounds? = null
        private set

    private var database: SQLiteDatabase? = SQLiteDatabase.openDatabase(
        file.absolutePath,
        null,
        SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
    )

    init {
        calculateZoomConstraints()
        calculateBounds()
    }

    override fun getTile(x: Int, y: Int, z: Int): Tile {
        val database = database
        if (!isZoomLevelAvailable(z) || database == null || !database.isOpen) {
            return TileProvider.NO_TILE
        }

        val row = (1 shl z) - y - 1
        database.query(
            "tiles",
            arrayOf("tile_data"),
            "tile_row = ? AND tile_column = ? AND zoom_level = ?",
            arrayOf(row.toString(), x.toString(), z.toString()),
            null,
            null,
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                Tile(256, 256, cursor.getBlob(0))
            } else {
                TileProvider.NO_TILE
            }
        }
    }

    override fun close() {
        database?.close()
        database = null
    }

    fun isZoomLevelAvailable(zoom: Int): Boolean {
        return zoom in minimumZoom..maximumZoom
    }

    private fun calculateZoomConstraints() {
        queryMetadata("minzoom") { minimumZoom = it.getInt(0) }
        queryMetadata("maxzoom") { maximumZoom = it.getInt(0) }
    }

    private fun calculateBounds() {
        queryMetadata("bounds") { cursor ->
            val (w, s, e, n) = cursor.getString(0).split(Regex(",\\s*")).take(4).map { it.toDouble() }
            bounds = LatLngBounds(LatLng(s, w), LatLng(n, e))
        }
    }

    private fun queryMetadata(name: String, onFound: (Cursor) -> Unit) {
        database!!.query("metadata", arrayOf("value"), "name = ?", arrayOf(name), null, null, null).use {
            if (it.moveToFirst()) {
                onFound(it)
            }
        }
    }
}
