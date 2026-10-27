package org.odk.collect.maplibre.staticmap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
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
    private val scope = CoroutineScope(Dispatchers.IO)

    // Makes concurrent requests for a session wait for the first one, so that only one session is created
    private val sessionMutex = Mutex()
    private var session: Session? = null

    fun getSession(apiKey: String, style: String?, callback: (String?) -> Unit) {
        scope.launch {
            val current = sessionMutex.withLock {
                session?.takeIf { it.style == style && it.expiryMillis > System.currentTimeMillis() }
                    ?: createSession(apiKey, style)?.also { session = it }
            }

            withContext(Dispatchers.Main) { callback(current?.token) }
        }
    }

    fun getTilesUrl(apiKey: String, session: String): String {
        return "https://tile.googleapis.com/v1/2dtiles/{z}/{x}/{y}?session=$session&key=$apiKey"
    }

    private fun createSession(apiKey: String, style: String?): Session? {
        return request("https://tile.googleapis.com/v1/createSession?key=$apiKey", requestBody(style)) {
            Session(it.getString("session"), TimeUnit.SECONDS.toMillis(it.getLong("expiry")), style)
        }
    }

    private fun <T> request(url: String, body: JSONObject? = null, parse: (JSONObject) -> T): T? {
        return try {
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

    private fun requestBody(style: String?): JSONObject {
        val locale = Locale.getDefault()
        val body = JSONObject()
            .put("language", locale.toLanguageTag())
            .put("region", locale.country.ifEmpty { "US" })

        return when (style) {
            MAP_TYPE_SATELLITE -> body.put("mapType", "satellite")
            MAP_TYPE_HYBRID -> body
                .put("mapType", "satellite")
                .put("layerTypes", JSONArray().put("layerRoadmap"))
            MAP_TYPE_TERRAIN -> body
                .put("mapType", "terrain")
                .put("layerTypes", JSONArray().put("layerRoadmap"))
            else -> body.put("mapType", "roadmap")
        }
    }

    private class Session(val token: String, val expiryMillis: Long, val style: String?)

    // Values of GoogleMap.MAP_TYPE_*, which is how the Google map style is stored in settings
    private const val MAP_TYPE_SATELLITE = "2"
    private const val MAP_TYPE_TERRAIN = "3"
    private const val MAP_TYPE_HYBRID = "4"

    private const val TIMEOUT = 10_000
}
