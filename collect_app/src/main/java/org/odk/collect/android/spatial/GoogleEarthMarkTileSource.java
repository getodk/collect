package org.odk.collect.android.spatial;

/**
 * @author jonnordling@gmail.com
 */
import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

public class GoogleEarthMarkTileSource extends OnlineTileSourceBase {
    public GoogleEarthMarkTileSource(String name, String[] baseurl) {
        super(name, string.unknown, 0, 20, 256, ".png", baseurl);
    }

    @Override
    public String getTileURLString(MapTile aTile) {
        return String.format(getBaseUrl(), aTile.getX(), aTile.getY(),
                aTile.getZoomLevel());
    }

}
