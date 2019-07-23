package org.odk.collect.android.geo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import timber.log.Timber;

/**
 * This class provides access to the metadata and tiles in a .mbtiles file.
 * An .mbtiles file is a SQLite database file containing specific tables and
 * columns, including tiles that may contain raster images or vector geometry.
 * See https://github.com/mapbox/mbtiles-spec for the detailed specification.
 */
class MbtilesFile implements Closeable, TileSource {
    public enum LayerType { RASTER, VECTOR }

    private final File file;
    private SQLiteDatabase db;
    private LayerType layerType;
    private final String contentType;
    private final String contentEncoding;

    MbtilesFile(File file) throws MbtilesException {
        this.file = file;
        if (!file.exists() || !file.isFile()) {
            throw new NotFileException(file);
        }

        // SQLite will create a "-journal" file for every file it touches, whether
        // or not it's a valid SQLite file; and if invalid, it will also create a
        // ".corrupt" file.  That means every time we scan some files to see whether
        // they are valid SQLite databases, SQLite will triple all the invalid files.
        // After several triplings, this quickly explodes into thousands of useless files.
        // Thus, we refuse to even attempt to open any "-journal" or ".corrupt" files.
        if (file.getName().endsWith("-journal") || file.getName().endsWith(".corrupt")) {
            throw new UnsupportedFilenameException(file);
        }
        try {
            db = SQLiteDatabase.openOrCreateDatabase(file, null);

            // The "format" code indicates whether the binary tiles are raster image
            // files (JPEG, PNG) or protobuf-encoded vector geometry (PBF, MVT).
            String format = getMetadata("format").toLowerCase(Locale.US);
            if (format.equals("pbf") || format.equals("mvt")) {
                contentType = "application/protobuf";
                contentEncoding = "gzip";
                layerType = LayerType.VECTOR;
            } else if (format.equals("jpg") || format.equals("jpeg")) {
                contentType = "image/jpeg";
                contentEncoding = "identity";
                layerType = LayerType.RASTER;
            } else if (format.equals("png")) {
                contentType = "image/png";
                contentEncoding = "identity";
                layerType = LayerType.RASTER;
            } else {
                db.close();
                throw new UnsupportedFormatException(format, file);
            }
        } catch (Throwable e) {
            throw new MbtilesException(e);
        }
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void close() {
        db.close();
    }

    /** Queries the "metadata" table, which has just "name" and "value" columns. */
    @NonNull String getMetadata(String key) throws MbtilesException {
        try (Cursor results = db.query("metadata", new String[] {"value"},
            "name = ?", new String[] {key}, null, null, null, null)) {
            return results.moveToFirst() ? results.getString(0) : "";
        } catch (Throwable e) {
            throw new MbtilesException(e);
        }
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
                    db = SQLiteDatabase.openOrCreateDatabase(file, null);
                }
            }
        } catch (Throwable e) {
            Timber.w(e);
        }
        return null;
    }

    /** Returns information about the vector layers available in the tiles. */
    List<VectorLayer> getVectorLayers() {
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

    /** Gets the internal name of an MBTiles file, or null if the file is invalid. */
    public static String getName(File file) {
        try {
            return new MbtilesFile(file).getMetadata("name");
        } catch (MbtilesException e) {
            return null;
        }
    }

    LayerType getLayerType() {
        return layerType;
    }

    /** Gets the layer type of an MBTiles file, or null if the file is invalid. */
    static LayerType getLayerType(File file) {
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

    static class MbtilesException extends IOException {
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
