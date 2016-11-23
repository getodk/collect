package org.odk.collect.android.spatial;

import android.content.Context;

import org.odk.collect.android.R;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;

/**
  * This factory augments Tile sources provides additional tile sources, some of which
  * are available in new version of osmdroid but not in currently used 4.2
 */
public class TileSourceFactory {
    public final OnlineTileSourceBase mUSGSTopo;
    public final OnlineTileSourceBase mUsgsSat;
    public final OnlineTileSourceBase mStamenTerrain;

    public TileSourceFactory(Context context) {
        mUSGSTopo = new OnlineTileSourceBase(
                context.getString(R.string.openmap_usgs_topo),
                ResourceProxy.string.unknown,
                0, 18, 256, "",
                new String[] { "http://basemap.nationalmap.gov/arcgis/rest/services/USGSTopo/MapServer/tile/" }) {
            @Override
            public String getTileURLString(MapTile aTile) {
                return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getY() + "/" + aTile.getX();
            }
        };

        mUsgsSat = new OnlineTileSourceBase(
                context.getString(R.string.openmap_usgs_sat),
                ResourceProxy.string.unknown,
                0, 18, 256, "",
                new String[]{"http://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryTopo/MapServer/tile/"}) {
            @Override
            public String getTileURLString(MapTile aTile) {
                return getBaseUrl() + aTile.getZoomLevel() + "/" + aTile.getY() + "/" + aTile.getX();
            }
        };

        mStamenTerrain = new XYTileSource(context.getString(R.string.openmap_stamen_terrain),
                ResourceProxy.string.unknown, 0, 18, 256, ".jpg", new String[] {
                "http://tile.stamen.com/terrain/" });
    }

    public OnlineTileSourceBase getUSGSTopo() {
        return mUSGSTopo;
    }

    public OnlineTileSourceBase getUsgsSat() {
        return mUsgsSat;
    }

    public OnlineTileSourceBase getStamenTerrain() {
        return mStamenTerrain;
    }
}
