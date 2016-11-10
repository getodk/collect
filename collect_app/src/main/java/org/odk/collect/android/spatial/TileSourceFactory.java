package org.odk.collect.android.spatial;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

/**
  * This factory augments Tile sources provides additional tile sources, some of which
  * are available in new version of osmdroid but not in currently used 4.2
 */
public class TileSourceFactory {
    public static final OnlineTileSourceBase USGS_TOPO = new OnlineTileSourceBase(
            "USGS National Map Topo",
            ResourceProxy.string.unknown,
            0, 18, 256, "",
            new String[] { "http://basemap.nationalmap.gov/arcgis/rest/services/USGSTopo/MapServer/tile/" }) {
        @Override
        public String getTileURLString(MapTile aTile) {
            return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getY() + "/" + aTile.getX();
        }
    };

    public static final OnlineTileSourceBase USGS_SAT = new OnlineTileSourceBase(
            "USGS National Map Sat",
            ResourceProxy.string.unknown,
            0, 18, 256, "",
            new String[]{"http://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryTopo/MapServer/tile/"}) {
        @Override
        public String getTileURLString(MapTile aTile) {
            return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getY() + "/" + aTile.getX();
        }
    };

    public static final OnlineTileSourceBase STAMEN_TERRAIN = new XYTileSource("Stamen Terrain",
            ResourceProxy.string.unknown, 0, 18, 256, ".jpg", new String[] {
            "http://tile.stamen.com/terrain/" });
}
