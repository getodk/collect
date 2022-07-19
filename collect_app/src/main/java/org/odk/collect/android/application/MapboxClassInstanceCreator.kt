package org.odk.collect.android.application

import android.os.Build
import androidx.fragment.app.Fragment
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.maps.MapFragment

object MapboxClassInstanceCreator {

    @JvmStatic
    fun isMapboxAvailable(): Boolean {
        val mapboxAbis = listOf(
            "arm64-v8a",
            "armeabi-v7a"
        )

        return createMapboxMapFragment() != null && mapboxAbis.contains(Build.SUPPORTED_ABIS.first())
    }

    fun createMapboxMapFragment(): MapFragment? {
        return createClassInstance<MapFragment>("org.odk.collect.mapbox.MapboxMapFragment")
    }

    @JvmStatic
    fun createMapBoxInitializationFragment(): Fragment? {
        return createClassInstance<Fragment>("org.odk.collect.mapbox.MapBoxInitializationFragment")
    }

    @JvmStatic
    fun createMapboxMapConfigurator(): MapConfigurator? {
        return createClassInstance<MapConfigurator>("org.odk.collect.mapbox.MapboxMapConfigurator")
    }

    private fun <T> createClassInstance(className: String): T? {
        return try {
            Class.forName(className).newInstance() as T
        } catch (e: ClassNotFoundException) {
            null
        } catch (e: IllegalAccessException) {
            null
        } catch (e: InstantiationException) {
            null
        }
    }
}
