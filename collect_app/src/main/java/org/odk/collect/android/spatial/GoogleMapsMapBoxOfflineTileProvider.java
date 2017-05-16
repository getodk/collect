/*
 * Copyright (C) 2016 GeoODK
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

package org.odk.collect.android.spatial;

/**
 * Created by jnordling on 12/29/15.
 *
 * @author jonnordling@gmail.com
 */

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.Closeable;
import java.io.File;

public class GoogleMapsMapBoxOfflineTileProvider implements TileProvider, Closeable {

    // ------------------------------------------------------------------------
    // Instance Variables
    // ------------------------------------------------------------------------

    private int minimumZoom = Integer.MIN_VALUE;

    private int maximumZoom = Integer.MAX_VALUE;

    private LatLngBounds bounds;

    private SQLiteDatabase database;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public GoogleMapsMapBoxOfflineTileProvider(File file) {
        this(file.getAbsolutePath());
    }

    public GoogleMapsMapBoxOfflineTileProvider(String pathToFile) {
        int flags = SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS;
        this.database = SQLiteDatabase.openDatabase(pathToFile, null, flags);
        this.calculateZoomConstraints();
        this.calculateBounds();
    }

    // ------------------------------------------------------------------------
    // TileProvider Interface
    // ------------------------------------------------------------------------

    @Override
    public Tile getTile(int x, int y, int z) {
        Tile tile = NO_TILE;
        if (this.isZoomLevelAvailable(z) && this.isDatabaseAvailable()) {
            String[] projection = {
                    "tile_data"
            };
            int row = ((int) (Math.pow(2, z) - y) - 1);
            String predicate = "tile_row = ? AND tile_column = ? AND zoom_level = ?";
            String[] values = {
                    String.valueOf(row), String.valueOf(x), String.valueOf(z)
            };
            Cursor c = this.database.query("tiles", projection, predicate, values, null, null,
                    null);
            if (c != null) {
                c.moveToFirst();
                if (!c.isAfterLast()) {
                    tile = new Tile(256, 256, c.getBlob(0));
                }
                c.close();
            }
        }
        return tile;
    }

    // ------------------------------------------------------------------------
    // Closeable Interface
    // ------------------------------------------------------------------------
    @Override
    public void close() {
        if (this.database != null) {
            this.database.close();
            this.database = null;
        }
    }

    // ------------------------------------------------------------------------
    // Public Methods
    // ------------------------------------------------------------------------


    public int getMinimumZoom() {
        return this.minimumZoom;
    }

    public int getMaximumZoom() {
        return this.maximumZoom;
    }


    public LatLngBounds getBounds() {
        return this.bounds;
    }

    public boolean isZoomLevelAvailable(int zoom) {
        return (zoom >= this.minimumZoom) && (zoom <= this.maximumZoom);
    }

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

    private void calculateZoomConstraints() {
        if (this.isDatabaseAvailable()) {

            String[] projection = new String[]{"value"};
            String[] minArgs = new String[]{"minzoom"};

            Cursor c = this.database.query("metadata", projection, "name = ?", minArgs, null, null, null);

            c.moveToFirst();
            if (!c.isAfterLast()) {
                this.minimumZoom = c.getInt(0);
            }
            c.close();

            String[] maxArgs = new String[]{"maxzoom"};
            c = this.database.query("metadata", projection, "name = ?", maxArgs, null, null, null);

            c.moveToFirst();
            if (!c.isAfterLast()) {
                this.maximumZoom = c.getInt(0);
            }
            c.close();
        }
    }

    private void calculateBounds() {
        if (this.isDatabaseAvailable()) {
            String[] projection = new String[]{
                    "value"
            };

            String[] subArgs = new String[]{
                    "bounds"
            };
            Cursor c = this.database.query("metadata", projection, "name = ?", subArgs, null, null,
                    null);
            c.moveToFirst();
            if (!c.isAfterLast()) {
                String[] parts = c.getString(0).split(",\\s*");

                double w = Double.parseDouble(parts[0]);
                double s = Double.parseDouble(parts[1]);
                double e = Double.parseDouble(parts[2]);
                double n = Double.parseDouble(parts[3]);

                LatLng ne = new LatLng(n, e);
                LatLng sw = new LatLng(s, w);

                this.bounds = new LatLngBounds(sw, ne);
            }
            c.close();
        }
    }

    private boolean isDatabaseAvailable() {
        return (this.database != null) && (this.database.isOpen());
    }

}