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

import android.graphics.drawable.Drawable;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.StreamUtils;

import java.io.InputStream;

import timber.log.Timber;


public class OsmMBTileModuleProvider extends MapTileFileStorageProviderBase {

    protected OsmMBTileSource tileSource;

    public OsmMBTileModuleProvider(IRegisterReceiver receiverRegistrar, OsmMBTileSource tileSource) {

        // Call the super constructor
        super(receiverRegistrar,
                Configuration.getInstance().getTileFileSystemThreads(),
                Configuration.getInstance().getTileFileSystemMaxQueueSize());

        // Initialize fields
        this.tileSource = tileSource;

    }

    @Override
    protected String getName() {
        return "MBTiles File Archive Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "mbtilesarchive";
    }

    @Override
    protected Runnable getTileLoader() {
        return new TileLoader();
    }

    @Override
    public boolean getUsesDataConnection() {
        return false;
    }

    @Override
    public int getMinimumZoomLevel() {
        return tileSource.getMinimumZoomLevel();
    }

    @Override
    public int getMaximumZoomLevel() {
        return tileSource.getMaximumZoomLevel();
    }

    @Override
    public void setTileSource(ITileSource tileSource) {
        Timber.w("*** Warning: someone's trying to reassign MBTileModuleProvider's tileSource!");
        if (tileSource instanceof OsmMBTileSource) {
            this.tileSource = (OsmMBTileSource) tileSource;
        } else {
        // logger.warn("*** Warning: and it wasn't even an MBTileSource! That's just rude!");

        }
    }

    private class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState state) {

            // if there's no sdcard then don't do anything
            if (!isSdCardAvailable()) {
                return null;
            }

            MapTile mapTile = state.getMapTile();
            InputStream inputStream = null;

            try {
                inputStream = tileSource.getInputStream(mapTile);

                if (inputStream != null) {

                    // Note that the finally clause will be called before
                    // the value is returned!
                    return tileSource.getDrawable(inputStream);
                }

            } catch (Throwable e) {
                Timber.e(e, "Error loading tile");

            } finally {
                if (inputStream != null) {
                    StreamUtils.closeStream(inputStream);
                }
            }

            return null;
        }
    }

}