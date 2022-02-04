package org.odk.collect.android.geo;

import org.odk.collect.android.application.Collect;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.MapTileIndex;

import java.io.Serializable;

import static org.odk.collect.strings.localization.LocalizedApplicationKt.getLocalizedString;

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
        this(getLocalizedString(Collect.getInstance(), cacheNameStringId),
            minZoomLevel, maxZoomLevel, tileSize, copyright, urlTemplates);
    }

    // Note: org.osmdroid.views.MapView.setTileSource takes an ITileSource,
    // but really it requires an instance of OnlineTileSourceBase.
    public OnlineTileSourceBase asOnlineTileSource() {
        if (onlineTileSource == null) {
            String extension = getExtension(urlTemplates[0]);
            onlineTileSource = new OnlineTileSourceBase(cacheName, minZoomLevel,
                maxZoomLevel, tileSize, extension, urlTemplates, copyright) {
                public String getTileURLString(long tileIndex) {
                    String urlTemplate = urlTemplates[random.nextInt(urlTemplates.length)];
                    return urlTemplate.replace("{x}", String.valueOf(MapTileIndex.getX(tileIndex)))
                        .replace("{y}", String.valueOf(MapTileIndex.getY(tileIndex)))
                        .replace("{z}", String.valueOf(MapTileIndex.getZoom(tileIndex)));
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
