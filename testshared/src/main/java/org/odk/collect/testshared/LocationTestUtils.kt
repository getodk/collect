package org.odk.collect.testshared

import android.location.Location

object LocationTestUtils {

    @JvmStatic
    fun createLocation(provider: String, lat: Double, lon: Double, alt: Double): FakeLocation {
        val location = FakeLocation(provider)
        location.latitude = lat
        location.longitude = lon
        location.altitude = alt
        return location
    }

    @JvmStatic
    fun createLocation(
        provider: String,
        lat: Double,
        lon: Double,
        alt: Double,
        sd: Float
    ): FakeLocation {
        val location = createLocation(provider, lat, lon, alt)
        location.accuracy = sd
        return location
    }

    @JvmStatic
    fun createLocation(
        provider: String,
        lat: Double,
        lon: Double,
        alt: Double,
        sd: Float,
        isLocationMocked: Boolean
    ): FakeLocation {
        val location = createLocation(provider, lat, lon, alt, sd)
        location.setIsFromMockProvider(isLocationMocked)
        return location
    }
}

/**
 * [Location] throws "Method ... not mocked" errors if you attempt to use it local tests. This fake
 * intercepts set/get calls and uses its own fields (in a similar way to Roboletric's "Shadows").
 *
 * @see [Method ... not mocked](https://tools.android.com/tech-docs/unit-testing-support#TOC-Method-...-not-mocked.-)
 */
class FakeLocation(provider: String?) : Location(provider) {

    private var _isFromMockProvider = false
    private var _provider: String? = provider
    private var _latitude: Double = 0.0
    private var _longitude: Double = 0.0
    private var _accuracy: Float? = null
    private var _altitude: Double = 0.0

    fun setIsFromMockProvider(isFromMockProvider: Boolean) {
        _isFromMockProvider = isFromMockProvider
    }

    override fun isFromMockProvider(): Boolean {
        return _isFromMockProvider
    }

    override fun getAltitude(): Double {
        return _altitude
    }

    override fun getAccuracy(): Float {
        return _accuracy ?: 0.0f
    }

    override fun getLatitude(): Double {
        return _latitude
    }

    override fun getLongitude(): Double {
        return _longitude
    }

    override fun getProvider(): String? {
        return _provider
    }

    override fun hasAccuracy(): Boolean {
        return _accuracy != null
    }

    override fun setLatitude(latitude: Double) {
        _latitude = latitude
    }

    override fun setLongitude(longitude: Double) {
        _longitude = longitude
    }

    override fun setProvider(provider: String?) {
        _provider = provider
    }

    override fun setAltitude(altitude: Double) {
        _altitude = altitude
    }

    override fun setAccuracy(horizontalAccuracy: Float) {
        _accuracy = horizontalAccuracy
    }
}
