package org.odk.collect.maplibre

import android.content.Context
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.module.http.HttpRequestUtil

object MapLibreSupport {
    const val GOOGLE_TILES_HOST = "tile.googleapis.com"

    private lateinit var applicationContext: Context

    val httpClient by lazy {
        OkHttpClient.Builder()
            .dispatcher(Dispatcher().apply { maxRequestsPerHost = 20 })
            .addInterceptor(AndroidAppHeadersInterceptor(applicationContext, GOOGLE_TILES_HOST))
            .build()
    }

    fun initialize(context: Context) {
        MapLibre.getInstance(
            context,
            MapboxAccessToken.get(context),
            WellKnownTileServer.Mapbox
        )

        // MapLibre makes no HTTP requests while the device is offline, and TileHttpServer serves
        // reference layers over HTTP, so they would never load offline without this.
        MapLibre.setConnected(true)

        applicationContext = context.applicationContext
        HttpRequestUtil.setOkHttpClient { httpClient.newCall(it) }
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
