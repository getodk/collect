package org.odk.collect.maplibre.staticmap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Client for the Google Map Tiles API. Creates the sessions that are needed to fetch Google tiles
 * and caches them in memory for the lifetime of the process.
 *
 * See https://developers.google.com/maps/documentation/tile/session_tokens
 */
object GoogleMapTilesClient {
    // Sessions are only accessed from one coroutine at a time, so they don't need synchronizing
    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(Dispatchers.IO.limitedParallelism(1))
    private val sessions = mutableMapOf<String?, Session>()

    fun getSession(apiKey: String, style: String?, callback: (String?) -> Unit) {
        scope.launch {
            val session = sessions[style]?.takeIf { it.expiry > System.currentTimeMillis() + EXPIRY_MARGIN }
                ?: createSession(apiKey, style)?.also { sessions[style] = it }

            withContext(Dispatchers.Main) { callback(session?.token) }
        }
    }

    private fun createSession(apiKey: String, style: String?): Session? {
        return try {
            val connection = URL("https://tile.googleapis.com/v1/createSession?key=$apiKey").openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = TIMEOUT
                connection.readTimeout = TIMEOUT
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(requestBody(style).toString().toByteArray()) }

                val response = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                Session(response.getString("session"), response.getString("expiry").toLong() * 1000)
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

    private class Session(val token: String, val expiry: Long)

    // Values of GoogleMap.MAP_TYPE_*, which is how the Google map style is stored in settings
    private const val MAP_TYPE_SATELLITE = "2"
    private const val MAP_TYPE_TERRAIN = "3"
    private const val MAP_TYPE_HYBRID = "4"

    // Covers the time it takes to fetch the tiles after the session is handed out
    private const val EXPIRY_MARGIN = 60 * 1000L
    private const val TIMEOUT = 10_000
}
