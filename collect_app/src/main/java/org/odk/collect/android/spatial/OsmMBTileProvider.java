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

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;

import java.io.File;
import java.util.Collections;


/**
 * This class is a simplification of the the MapTileProviderArray: it only
 * allows a single provider.
 */
public class OsmMBTileProvider extends MapTileProviderArray {

    public OsmMBTileProvider(IRegisterReceiver receiverRegistrar, File file) {

        /**
         * Call the super-constructor.
         *
         * MapTileProviderBase requires a TileSource. As far as I can tell it is
         * only used in its method rescaleCache(...) to get the pixel size of a
         * tile. It seems to me that this is inappropriate, as a MapTileProvider
         * can have multiple sources (like the module array defined below) and
         * therefore multiple tileSources which might return different values!!
         *
         * If the requirement is that the tile size is equal across tile
         * sources, then the parameter should be obtained from a different
         * location, From TileSystem for example.
         */
        super(OsmMBTileSource.createFromFile(file), receiverRegistrar);

        // Create the module provider; this class provides a TileLoader that
        // actually loads the tile from the DB.
        OsmMBTileModuleProvider moduleProvider;
        moduleProvider = new OsmMBTileModuleProvider(receiverRegistrar, (OsmMBTileSource) getTileSource());

        MapTileModuleProviderBase[] tileProviderArray;
        tileProviderArray = new MapTileModuleProviderBase[]{moduleProvider};

        // Add the module provider to the array of providers; mTileProviderList
        // is defined by the superclass.
        Collections.addAll(mTileProviderList, tileProviderArray);
    }

    // TODO: implement public Drawable getMapTile(final MapTile pTile) {}
    //       The current implementation is needlessly complex because it uses
    //       MapTileProviderArray as a basis.

}