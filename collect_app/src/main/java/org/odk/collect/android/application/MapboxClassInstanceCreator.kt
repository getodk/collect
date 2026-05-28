package org.odk.collect.android.application

import androidx.fragment.app.Fragment
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.maps.MapFragment

object MapboxClassInstanceCreator {

    private const val MAP_FRAGMENT = "org.odk.collect.mapbox.MapboxMapFragment"

    @JvmStatic
    fun isMapboxAvailable(): Boolean {
        return try {
            Class.forName(MAP_FRAGMENT)
            System.loadLibrary("mapbox-common")
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun createMapboxMapFragment(configuration: String): MapFragment {
        return Class.forName(MAP_FRAGMENT)
            .getConstructor(String::class.java)
            .newInstance(configuration) as MapFragment
    }

    @JvmStatic
    fun createMapBoxInitializationFragment(): Fragment {
        return Class.forName("org.odk.collect.mapbox.MapBoxInitializationFragment")
            .getConstructor()
            .newInstance() as Fragment
    }

    @JvmStatic
    fun createMapboxMapConfigurator(configuration: String): MapConfigurator {
        return Class.forName("org.odk.collect.mapbox.MapboxMapConfigurator")
            .getConstructor(String::class.java)
            .newInstance(configuration) as MapConfigurator
    }

}
