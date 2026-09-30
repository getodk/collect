package org.odk.collect.maplibre

import android.content.Context
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

object MapLibreSupport {

    fun initialize(context: Context) {
        MapLibre.getInstance(
            context,
            MapboxAccessToken.get(context),
            WellKnownTileServer.Mapbox
        )

        // MapLibre makes no HTTP requests while the device is offline, and TileHttpServer serves
        // reference layers over HTTP, so they would never load offline without this.
        MapLibre.setConnected(true)
    }

    /**
     * The native library is only packaged for ARM to keep the APK small, so on other devices
     * MapLibre throws an error as soon as it starts building a map.
     */
    @JvmStatic
    fun isAvailable(): Boolean {
        return try {
            System.loadLibrary("maplibre")
            true
        } catch (_: Throwable) {
            false
        }
    }
}
