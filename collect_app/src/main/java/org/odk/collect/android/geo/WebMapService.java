package org.odk.collect.android.geo;

import org.odk.collect.android.application.Collect;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

import java.io.Serializable;

/**
 * A serializable definition of a Web Map Service in terms of its URL structure
 * and the parameters for fetching tiles from it.
 */
class WebMapService implements Serializable {
    public final String cacheName;
    public final int minZoomLevel;
    public final int maxZoomLevel;
    public final int tileSize;
    public final String copyright;
    public final String[] urlTemplates;

    private OnlineTileSourceBase onlineTileSource;

    WebMapService(String cacheName, int minZoomLevel, int maxZoomLevel,
        int tileSize, String copyright, String... urlTemplates) {
        this.cacheName = cacheName;
        this.minZoomLevel = minZoomLevel;
        this.maxZoomLevel = maxZoomLevel;
        this.tileSize = tileSize;
        this.copyright = copyright;
        this.urlTemplates = urlTemplates;
    }

    @Deprecated WebMapService(int cacheNameStringId, int minZoomLevel,
        int maxZoomLevel, int tileSize, String copyright, String... urlTemplates) {
        this(Collect.getInstance().getString(cacheNameStringId),
            minZoomLevel, maxZoomLevel, tileSize, copyright, urlTemplates);
    }

    // Note: org.osmdroid.views.MapView.setTileSource takes an ITileSource,
    // but really it requires an instance of OnlineTileSourceBase.
    public OnlineTileSourceBase asOnlineTileSource() {
        if (onlineTileSource == null) {
            String extension = getExtension(urlTemplates[0]);
            onlineTileSource = new OnlineTileSourceBase(cacheName, minZoomLevel,
                maxZoomLevel, tileSize, extension, urlTemplates, copyright) {
                public String getTileURLString(MapTile tile) {
                    String urlTemplate = urlTemplates[random.nextInt(urlTemplates.length)];
                    return urlTemplate.replace("{x}", String.valueOf(tile.getX()))
                        .replace("{y}", String.valueOf(tile.getY()))
                        .replace("{z}", String.valueOf(tile.getZoomLevel()));
                }
            };
        }
        return onlineTileSource;
    }

    private String getExtension(String urlTemplate) {
        String[] parts = urlTemplate.split("/");
        String lastPart = parts[parts.length - 1];
        if (lastPart.contains(".")) {
            String[] subparts = lastPart.split("\\.");
            return "." + subparts[subparts.length - 1];
        }
        return "";
    }
}
