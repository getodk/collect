package org.odk.collect.maps.layers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static android.database.sqlite.SQLiteDatabase.NO_LOCALIZED_COLLATORS;
import static android.database.sqlite.SQLiteDatabase.OPEN_READONLY;

/**
 * This class provides access to the metadata and tiles in an .mbtiles file.
 * An .mbtiles file is a SQLite database file containing specific tables and
 * columns, including tiles that may contain raster images or vector geometry.
 * See https://github.com/mapbox/mbtiles-spec for the detailed specification.
 */
public class MbtilesFile implements Closeable, TileSource {
    public enum LayerType { RASTER, VECTOR }

    private final File file;
    private final LayerType layerType;
    private final String contentType;
    private final String contentEncoding;
    private SQLiteDatabase db;  // see getTileBlob for why this is not final

    public MbtilesFile(File file) throws MbtilesException {
        this(file, detectContentType(file));
    }

    private MbtilesFile(File file, String contentType) throws MbtilesException {
        this.file = file;
        this.db = openSqliteReadOnly(file);
        this.contentType = contentType;
        switch (contentType) {
            case "application/protobuf":
                contentEncoding = "gzip";
                layerType = LayerType.VECTOR;
                return;
            case "image/jpeg":
            case "image/png":
                contentEncoding = "identity";
                layerType = LayerType.RASTER;
                return;
        }
        throw new MbtilesException(String.format(
            "Unrecognized content type \"%s\" in %s", contentType, file));
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public LayerType getLayerType() {
        return layerType;
    }

    public @NonNull String getMetadata(String key) throws MbtilesException {
        return queryMetadata(db, key);
    }

    public void close() {
        db.close();
    }

    /** Fetches a tile out of the .mbtiles SQLite database. */
    // PMD complains about returning null for an array return type, but we
    // really do want to return null when there is no tile available.
    @SuppressWarnings("PMD.ReturnEmptyArrayRatherThanNull")
    public byte[] getTileBlob(int zoom, int x, int y) {
        // TMS coordinates are used in .mbtiles files, so Y needs to be flipped.
        y = (1 << zoom) - 1 - y;

        // We have to use String.format because the templating mechanism in
        // SQLiteDatabase.query is written for a strange alternate universe
        // in which numbers don't exist -- it only supports strings!
        String selection = String.format(
            Locale.US,
            "zoom_level = %d and tile_column = %d and tile_row = %d",
            zoom, x, y
        );

        try (Cursor results = db.query("tiles", new String[] {"tile_data"},
            selection, null, null, null, null)) {
            if (results.moveToFirst()) {
                try {
                    return results.getBlob(0);
                } catch (IllegalStateException e) {
                    Timber.w(e, "Could not select tile data at zoom=%d, x=%d, y=%d", zoom, x, y);
                    // In Android, the SQLite cursor can handle at most 2 MB in one row;
                    // exceeding 2 MB in an .mbtiles file is rare, but it can happen.
                    // When an attempt to fetch a large row fails, the database ends up
                    // in an unusable state, so we need to close it and reopen it.
                    // See https://stackoverflow.com/questions/20094421/cursor-window-window-is-full
                    db.close();
                    db = openSqliteReadOnly(file);
                }
            }
        } catch (Throwable e) {
            Timber.w(e);
        }
        return null;
    }

    /** Returns information about the vector layers available in the tiles. */
    public List<VectorLayer> getVectorLayers() {
        List<VectorLayer> layers = new ArrayList<>();
        JSONArray jsonLayers;
        try {
            JSONObject json = new JSONObject(getMetadata("json"));
            jsonLayers = json.getJSONArray("vector_layers");
            for (int i = 0; i < jsonLayers.length(); i++) {
                layers.add(new VectorLayer(jsonLayers.getJSONObject(i)));
            }
        } catch (MbtilesException | JSONException e) {
            Timber.w(e);
        }
        return layers;
    }

    /** Reads the internal name from an MBTiles file, or null if the file is invalid. */
    public static String readName(File file) {
        try {
            return new MbtilesFile(file).getMetadata("name");
        } catch (MbtilesException e) {
            return null;
        }
    }

    /** Reads the layer type from an MBTiles file, or null if the file is invalid. */
    public static LayerType readLayerType(File file) {
        try {
            return new MbtilesFile(file).getLayerType();
        } catch (MbtilesException e) {
            return null;
        }
    }

    /** Vector layer metadata.  See https://github.com/mapbox/mbtiles-spec for details. */
    public static class VectorLayer {
        public final String name;

        VectorLayer(JSONObject json) {
            name = json.optString("id", "");
        }
    }

    /** Reads or guesses the tile data content type in an .mbtiles file. */
    private static String detectContentType(File file) throws MbtilesException {
        if (!file.exists() || !file.isFile()) {
            throw new NotFileException(file);
        }
        if (!file.getName().toLowerCase(Locale.US).endsWith(".mbtiles")) {
            throw new UnsupportedFilenameException(file);
        }
        try (SQLiteDatabase db = openSqliteReadOnly(file)) {
            // The "format" code indicates whether the binary tiles are raster image
            // files (JPEG, PNG) or protobuf-encoded vector geometry (PBF, MVT).
            String format = queryMetadata(db, "format");
            switch (format.toLowerCase(Locale.US)) {
                case "pbf":
                case "mvt":
                    return "application/protobuf";
                case "jpg":
                case "jpeg":
                    return "image/jpeg";
                case "png":
                    return "image/png";
            }

            // We have seen some raster .mbtiles files in the wild that are
            // missing the "format" field, so let's attempt autodetection.
            byte[] tileHeader = queryAnyTileHeader(db);
            if (startsWithBytes(tileHeader, 0xff, 0xd8, 0xff, 0xe0)) {
                return "image/jpeg";
            }
            if (startsWithBytes(tileHeader, 0x89, 'P', 'N', 'G')) {
                return "image/png";
            }
            if (startsWithBytes(tileHeader, 0x1f, 0x8b)) {  // gzip header
                return "application/protobuf";
            }
            throw new UnsupportedFormatException(format, file);
        } catch (Throwable e) {
            throw new MbtilesException(e);
        }
    }

    private static SQLiteDatabase openSqliteReadOnly(File file) {
        return SQLiteDatabase.openDatabase(
            file.getPath(), null, OPEN_READONLY | NO_LOCALIZED_COLLATORS);
    }

    private static boolean startsWithBytes(byte[] actual, int... expected) {
        int count = 0;
        for (int i = 0; i < actual.length && i < expected.length; i++) {
            count += (actual[i] == (byte) expected[i]) ? 1 : 0;
        }
        return count == expected.length;
    }

    /** Queries the "metadata" table, which has just "name" and "value" columns. */
    private static @NonNull String queryMetadata(SQLiteDatabase db, String key) throws MbtilesException {
        try (Cursor results = db.query("metadata", new String[] {"value"},
            "name = ?", new String[] {key}, null, null, null, "1")) {
            return results.moveToFirst() ? results.getString(0) : "";
        } catch (Throwable e) {
            throw new MbtilesException(e);
        }
    }

    /** Fetches the first 16 bytes of any tile blob found in the "tiles" table. */
    private static @NonNull byte[] queryAnyTileHeader(SQLiteDatabase db) throws MbtilesException {
        try (Cursor results = db.query("tiles", new String[] {"substr(tile_data, 1, 16)"},
            null, null, null, null, null, "1")) {
            return results.moveToFirst() ? results.getBlob(0) : new byte[0];
        } catch (Throwable e) {
            throw new MbtilesException(e);
        }
    }

    public static class MbtilesException extends IOException {
        MbtilesException(Throwable cause) {
            this(cause.getMessage());
            initCause(cause);
        }

        MbtilesException(String message) {
            super(message);
        }
    }

    public static class NotFileException extends MbtilesException {
        NotFileException(File file) {
            super("Not a file: " + file);
        }
    }

    public static class UnsupportedFilenameException extends MbtilesException {
        UnsupportedFilenameException(File file) {
            super("Illegal filename for SQLite file: " + file);
        }
    }

    public static class UnsupportedFormatException extends MbtilesException {
        UnsupportedFormatException(String format, File file) {
            super(String.format("Unrecognized .mbtiles format \"%s\" in %s", format, file));
        }
    }
}
