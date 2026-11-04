package org.odk.collect.maplibre.staticmap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.geometry.LatLngBounds
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Client for the Google Map Tiles API. Creates the session that is needed to fetch Google tiles
 * and keeps it in memory until it expires or the style changes.
 *
 * See https://developers.google.com/maps/documentation/tile/session_tokens
 */
object GoogleMapTilesClient {
    // Makes concurrent requests for a session wait for the first one, so that only one session is created
    private val sessionMutex = Mutex()
    private var session: Session? = null

    /**
     * Returns the URL of the tiles and the copyright for the area within [bounds] at [zoom], or
     * null if either can't be fetched.
     */
    suspend fun getTiles(apiKey: String, style: String?, zoom: Int, bounds: LatLngBounds): Tiles? {
        val currentSession = sessionMutex.withLock {
            val existingSession = session?.takeIf { it.style == style && it.expiryMillis > System.currentTimeMillis() }
            existingSession ?: createSession(apiKey, style)?.also { session = it }
        } ?: return null

        val copyright = getCopyright(apiKey, currentSession.token, zoom, bounds) ?: return null
        return Tiles("https://$HOST/v1/2dtiles/{z}/{x}/{y}?session=${currentSession.token}&key=$apiKey", copyright)
    }

    private suspend fun getCopyright(apiKey: String, session: String, zoom: Int, bounds: LatLngBounds): String? {
        val url = "https://$HOST/tile/v1/viewport?session=$session&key=$apiKey&zoom=$zoom" +
            "&north=${bounds.latitudeNorth}&south=${bounds.latitudeSouth}&east=${bounds.longitudeEast}&west=${bounds.longitudeWest}"
        return request(url) { it.optString("copyright") }
    }

    private suspend fun createSession(apiKey: String, style: String?): Session? {
        val locale = Locale.getDefault()
        val body = JSONObject()
            .put("language", locale.toLanguageTag())
            .put("region", locale.country.ifEmpty { "US" })

        when (style) {
            MAP_TYPE_SATELLITE -> body.put("mapType", "satellite")
            MAP_TYPE_HYBRID -> body
                .put("mapType", "satellite")
                .put("layerTypes", JSONArray().put("layerRoadmap"))
            MAP_TYPE_TERRAIN -> body
                .put("mapType", "terrain")
                .put("layerTypes", JSONArray().put("layerRoadmap"))
            else -> body.put("mapType", "roadmap")
        }

        return request("https://$HOST/v1/createSession?key=$apiKey", body) {
            Session(it.getString("session"), TimeUnit.SECONDS.toMillis(it.getLong("expiry")), style)
        }
    }

    private suspend fun <T> request(url: String, body: JSONObject? = null, parse: (JSONObject) -> T): T? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = TIMEOUT
                    connection.readTimeout = TIMEOUT
                    if (body != null) {
                        connection.doOutput = true
                        connection.setRequestProperty("Content-Type", "application/json")
                        connection.outputStream.use { it.write(body.toString().toByteArray()) }
                    }

                    parse(JSONObject(connection.inputStream.bufferedReader().use { it.readText() }))
                } finally {
                    connection.disconnect()
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    class Tiles(val url: String, val copyright: String)

    private class Session(val token: String, val expiryMillis: Long, val style: String?)

    // Values of GoogleMap.MAP_TYPE_*, which is how the Google map style is stored in settings
    private const val MAP_TYPE_SATELLITE = "2"
    private const val MAP_TYPE_TERRAIN = "3"
    private const val MAP_TYPE_HYBRID = "4"

    private const val TIMEOUT = 10_000

    const val HOST = "tile.googleapis.com"
}
