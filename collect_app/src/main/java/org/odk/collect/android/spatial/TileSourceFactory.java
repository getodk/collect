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
    public final OnlineTileSourceBase mCartoDbPositron;
    public final OnlineTileSourceBase mCartoDbDarkMatter;

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

        mCartoDbPositron = new XYTileSource(context.getString(R.string.openmap_cartodb_positron),
                ResourceProxy.string.unknown, 0, 18, 256, ".png", new String[] {
                "http://1.basemaps.cartocdn.com/light_all/" });

        mCartoDbDarkMatter = new XYTileSource(context.getString(R.string.openmap_cartodb_positron),
                ResourceProxy.string.unknown, 0, 18, 256, ".png", new String[] {
                "http://1.basemaps.cartocdn.com/dark_all/" });
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

    public OnlineTileSourceBase getCartoDbPositron() {
        return mCartoDbPositron;
    }

    public OnlineTileSourceBase getCartoDbDarkMatter() {
        return mCartoDbDarkMatter;
    }
}
