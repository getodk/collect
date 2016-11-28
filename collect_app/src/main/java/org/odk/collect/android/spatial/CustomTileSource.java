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

import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;

public class CustomTileSource extends BitmapTileSourceBase {

    //public CustomTileSource(String aName, string aResourceId,int aZoomMinLevel, int
    // aZoomMaxLevel, int aTileSizePixels,	String aImageFilenameEnding) {
    public CustomTileSource(String aName, string aResourceId) {
        //super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
        // aImageFilenameEnding);
        super(aName, aResourceId, 1, 6, 256, ".png");
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getTileRelativeFilenameString(MapTile tile) {
        final StringBuilder sb = new StringBuilder();
        sb.append(pathBase());
        sb.append('/');
        sb.append(tile.getZoomLevel());
        sb.append('/');
        sb.append(tile.getX());
        sb.append('_');
        sb.append(tile.getY());
        sb.append(imageFilenameEnding());
        return sb.toString();

    }

}
