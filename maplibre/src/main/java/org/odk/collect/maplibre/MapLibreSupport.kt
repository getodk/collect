package org.odk.collect.maplibre

object MapLibreSupport {

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
