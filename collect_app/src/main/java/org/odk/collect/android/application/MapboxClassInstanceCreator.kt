package org.odk.collect.android.application

import androidx.fragment.app.Fragment
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.maps.MapFragment

object MapboxClassInstanceCreator {

    @JvmStatic
    fun isMapboxAvailable(): Boolean {
        return createMapboxMapFragment() != null && try {
            System.loadLibrary("mapbox-common")
            true
        } catch (e: Throwable) {
            false
        }
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
