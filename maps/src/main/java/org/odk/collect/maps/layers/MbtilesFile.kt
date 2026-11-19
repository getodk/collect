package org.odk.collect.maps.layers

import android.database.sqlite.SQLiteDatabase
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.util.Locale

/**
 * This class provides access to the metadata and tiles in an .mbtiles file.
 * An .mbtiles file is a SQLite database file containing specific tables and
 * columns, including tiles that may contain raster images or vector geometry.
 * See https://github.com/mapbox/mbtiles-spec for the detailed specification.
 */
class MbtilesFile private constructor(
    private val file: File,
    private val contentType: String
) : Closeable, TileSource {

    enum class LayerType { RASTER, VECTOR }

    val layerType: LayerType
    private val contentEncoding: String
    private var db: SQLiteDatabase = openSqliteReadOnly(file)

    @Throws(MbtilesException::class)
    constructor(file: File) : this(file, detectContentType(file))

    init {
        when (contentType) {
            "application/protobuf" -> {
                contentEncoding = "gzip"
                layerType = LayerType.VECTOR
            }
            "image/jpeg", "image/png" -> {
                contentEncoding = "identity"
                layerType = LayerType.RASTER
            }
            else -> throw MbtilesException("Unrecognized content type \"$contentType\" in $file")
        }
    }

    override fun getContentType(): String = contentType

    override fun getContentEncoding(): String = contentEncoding

    @Throws(MbtilesException::class)
    fun getMetadata(key: String): String = queryMetadata(db, key)

    /** Returns the "minzoom" metadata value, or null if it is missing or invalid. */
    val minZoom: Int?
        get() = getZoomMetadata("minzoom")

    /** Returns the "maxzoom" metadata value, or null if it is missing or invalid. */
    val maxZoom: Int?
        get() = getZoomMetadata("maxzoom")

    private fun getZoomMetadata(key: String): Int? {
        return try {
            getMetadata(key).toDoubleOrNull()?.toInt()
        } catch (_: MbtilesException) {
            null
        }
    }

    override fun close() {
        db.close()
    }

    /** Fetches a tile out of the .mbtiles SQLite database. */
    override fun getTileBlob(zoom: Int, x: Int, y: Int): ByteArray? {
        // TMS coordinates are used in .mbtiles files, so Y needs to be flipped.
        val row = (1 shl zoom) - 1 - y

        // We have to use String.format because the templating mechanism in
        // SQLiteDatabase.query is written for a strange alternate universe
        // in which numbers don't exist -- it only supports strings!
        val selection = String.format(
            Locale.US,
            "zoom_level = %d and tile_column = %d and tile_row = %d",
            zoom,
            x,
            row
        )

        try {
            db.query("tiles", arrayOf("tile_data"), selection, null, null, null, null).use { results ->
                if (results.moveToFirst()) {
                    try {
                        return results.getBlob(0)
                    } catch (e: IllegalStateException) {
                        Timber.w(e, "Could not select tile data at zoom=%d, x=%d, y=%d", zoom, x, row)
                        // In Android, the SQLite cursor can handle at most 2 MB in one row;
                        // exceeding 2 MB in an .mbtiles file is rare, but it can happen.
                        // When an attempt to fetch a large row fails, the database ends up
                        // in an unusable state, so we need to close it and reopen it.
                        // See https://stackoverflow.com/questions/20094421/cursor-window-window-is-full
                        db.close()
                        db = openSqliteReadOnly(file)
                    }
                }
            }
        } catch (e: Throwable) {
            Timber.w(e)
        }
        return null
    }

    /** Returns information about the vector layers available in the tiles. */
    val vectorLayers: List<VectorLayer>
        get() {
            val layers = mutableListOf<VectorLayer>()
            try {
                val jsonLayers = JSONObject(getMetadata("json")).getJSONArray("vector_layers")
                for (i in 0 until jsonLayers.length()) {
                    layers.add(VectorLayer(jsonLayers.getJSONObject(i)))
                }
            } catch (e: MbtilesException) {
                Timber.w(e)
            } catch (e: JSONException) {
                Timber.w(e)
            }
            return layers
        }

    /** Vector layer metadata.  See https://github.com/mapbox/mbtiles-spec for details. */
    class VectorLayer internal constructor(json: JSONObject) {
        val name: String = json.optString("id", "")
    }

    open class MbtilesException : IOException {
        internal constructor(message: String?) : super(message)

        internal constructor(cause: Throwable) : this(cause.message) {
            initCause(cause)
        }
    }

    class NotFileException internal constructor(file: File) :
        MbtilesException("Not a file: $file")

    class UnsupportedFilenameException internal constructor(file: File) :
        MbtilesException("Illegal filename for SQLite file: $file")

    class UnsupportedFormatException internal constructor(format: String, file: File) :
        MbtilesException("Unrecognized .mbtiles format \"$format\" in $file")

    companion object {
        const val FILE_EXTENSION = ".mbtiles"

        /** Reads the internal name from an MBTiles file, or null if the file is invalid. */
        @JvmStatic
        fun readName(file: File): String? {
            return try {
                MbtilesFile(file).getMetadata("name")
            } catch (_: MbtilesException) {
                null
            }
        }

        /** Reads the layer type from an MBTiles file, or null if the file is invalid. */
        @JvmStatic
        fun readLayerType(file: File): LayerType? {
            return try {
                MbtilesFile(file).layerType
            } catch (_: MbtilesException) {
                null
            }
        }

        /** Reads or guesses the tile data content type in an .mbtiles file. */
        private fun detectContentType(file: File): String {
            if (!file.exists() || !file.isFile) {
                throw NotFileException(file)
            }
            if (!file.name.lowercase(Locale.US).endsWith(FILE_EXTENSION)) {
                throw UnsupportedFilenameException(file)
            }
            try {
                openSqliteReadOnly(file).use { db ->
                    // The "format" code indicates whether the binary tiles are raster image
                    // files (JPEG, PNG) or protobuf-encoded vector geometry (PBF, MVT).
                    val format = queryMetadata(db, "format")
                    when (format.lowercase(Locale.US)) {
                        "pbf", "mvt" -> return "application/protobuf"
                        "jpg", "jpeg" -> return "image/jpeg"
                        "png" -> return "image/png"
                    }

                    // We have seen some raster .mbtiles files in the wild that are
                    // missing the "format" field, so let's attempt autodetection.
                    val tileHeader = queryAnyTileHeader(db)
                    if (startsWithBytes(tileHeader, 0xff, 0xd8, 0xff, 0xe0)) {
                        return "image/jpeg"
                    }
                    if (startsWithBytes(tileHeader, 0x89, 'P'.code, 'N'.code, 'G'.code)) {
                        return "image/png"
                    }
                    if (startsWithBytes(tileHeader, 0x1f, 0x8b)) { // gzip header
                        return "application/protobuf"
                    }
                    throw UnsupportedFormatException(format, file)
                }
            } catch (e: Throwable) {
                throw MbtilesException(e)
            }
        }

        private fun openSqliteReadOnly(file: File): SQLiteDatabase {
            return SQLiteDatabase.openDatabase(
                file.path,
                null,
                SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
            )
        }

        private fun startsWithBytes(actual: ByteArray, vararg expected: Int): Boolean {
            var count = 0
            var i = 0
            while (i < actual.size && i < expected.size) {
                count += if (actual[i] == expected[i].toByte()) 1 else 0
                i++
            }
            return count == expected.size
        }

        /** Queries the "metadata" table, which has just "name" and "value" columns. */
        private fun queryMetadata(db: SQLiteDatabase, key: String): String {
            try {
                db.query("metadata", arrayOf("value"), "name = ?", arrayOf(key), null, null, null, "1").use { results ->
                    return if (results.moveToFirst()) results.getString(0) ?: "" else ""
                }
            } catch (e: Throwable) {
                throw MbtilesException(e)
            }
        }

        /** Fetches the first 16 bytes of any tile blob found in the "tiles" table. */
        private fun queryAnyTileHeader(db: SQLiteDatabase): ByteArray {
            try {
                db.query("tiles", arrayOf("substr(tile_data, 1, 16)"), null, null, null, null, null, "1").use { results ->
                    return if (results.moveToFirst()) results.getBlob(0) ?: ByteArray(0) else ByteArray(0)
                }
            } catch (e: Throwable) {
                throw MbtilesException(e)
            }
        }
    }
}
