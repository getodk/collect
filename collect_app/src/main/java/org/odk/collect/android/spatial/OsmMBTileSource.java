/*
 * Copyright (C) 2014 GeoODK
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * @author Jon Nordling (jonnordling@gmail.com)
 */

package org.odk.collect.android.spatial;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import timber.log.Timber;

public class OsmMBTileSource extends BitmapTileSourceBase {

    // Log log log log ...
    // private static final Logger logger = LoggerFactory.getLogger(MBTileSource.class);
    // Database related fields
    public static final String TABLE_TILES = "tiles";
    public static final String COL_TILES_ZOOM_LEVEL = "zoom_level";
    public static final String COL_TILES_TILE_COLUMN = "tile_column";
    public static final String COL_TILES_TILE_ROW = "tile_row";
    public static final String COL_TILES_TILE_DATA = "tile_data";

    protected SQLiteDatabase database;
    protected File archive;

    // Reasonable defaults ..
    public static final int MIN_ZOOM = 8;
    public static final int MAX_ZOOM = 15;
    public static final int TILE_SIZE_PIXELS = 256;

    /**
     * The reason this constructor is protected is because all parameters,
     * except file should be determined from the archive file. Therefore a
     * factory method is necessary.
     */
    protected OsmMBTileSource(int minZoom,
                              int maxZoom,
                              int tileSizePixels,
                              File file,
                              SQLiteDatabase db) {
        super("MBTiles", minZoom, maxZoom, tileSizePixels, ".png");

        archive = file;
        database = db;
    }

    /**
     * Creates a new MBTileSource from file.
     * <p>
     * Parameters minZoom, maxZoom en tileSizePixels are obtained from the
     * database. If they cannot be obtained from the DB, the default values as
     * defined by this class are used.
     */
    public static OsmMBTileSource createFromFile(File file) {
        int flags = SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY;
        int tileSize = TILE_SIZE_PIXELS;

        // Open the database
        SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, flags);

        // Get the tile size
        Cursor cursor = db.rawQuery("SELECT tile_data FROM tiles LIMIT 0,1",
                new String[]{});

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            InputStream is = new ByteArrayInputStream(cursor.getBlob(0));

            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                tileSize = bitmap.getHeight();
            }
            Timber.w("Found a tile size of %d", tileSize);
        }
        cursor.close();

        // Get the minimum zoomlevel from the MBTiles file
        int value = getInt(db, "SELECT MIN(zoom_level) FROM tiles;");
        int minZoomLevel = value > -1 ? value : MIN_ZOOM;

        // Get the maximum zoomlevel from the MBTiles file
        value = getInt(db, "SELECT MAX(zoom_level) FROM tiles;");
        int maxZoomLevel = value > -1 ? value : MAX_ZOOM;

        return new OsmMBTileSource(minZoomLevel, maxZoomLevel, tileSize, file, db);
    }

    protected static int getInt(SQLiteDatabase db, String sql) {
        Cursor cursor = db.rawQuery(sql, new String[]{});
        int value = -1;

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            value = cursor.getInt(0);
            Timber.e("Found a minimum zoomlevel of %d", value);
        }

        cursor.close();
        return value;
    }

    public InputStream getInputStream(MapTile mapTile) {

        try {
            InputStream ret = null;
            final String[] tile = {COL_TILES_TILE_DATA};
            final String[] xyz = {Integer.toString(mapTile.getX()),
                    Double.toString(Math.pow(2, mapTile.getZoomLevel()) - mapTile.getY() - 1),
                    Integer.toString(mapTile.getZoomLevel())};

            final Cursor cur = database.query(TABLE_TILES,
                    tile,
                    "tile_column=? and tile_row=? and zoom_level=?",
                    xyz,
                    null,
                    null,
                    null);

            if (cur.getCount() != 0) {
                cur.moveToFirst();
                ret = new ByteArrayInputStream(cur.getBlob(0));
            }

            cur.close();

            if (ret != null) {
                return ret;
            }

        } catch (final Throwable e) {
            Timber.w(e, "Error getting db stream: %s", mapTile);
        }
        return null;
    }
}