package org.odk.collect.android.application

import androidx.fragment.app.Fragment
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.maps.MapFragment

object MapboxClassInstanceCreator {
    fun createMapboxMapFragment(): MapFragment? {
        return try {
            Class.forName("org.odk.collect.mapbox.MapboxMapFragment").newInstance() as MapFragment
        } catch (e: ClassNotFoundException) {
            null
        } catch (e: IllegalAccessException) {
            null
        } catch (e: InstantiationException) {
            null
        }
    }

    @JvmStatic
    fun createMapBoxInitializationFragment(): Fragment? {
        return try {
            Class.forName("org.odk.collect.mapbox.MapBoxInitializationFragment").newInstance() as Fragment
        } catch (e: ClassNotFoundException) {
            null
        } catch (e: IllegalAccessException) {
            null
        } catch (e: InstantiationException) {
            null
        }
    }

    @JvmStatic
    fun createMapboxMapConfigurator(): MapConfigurator? {
        return try {
            Class.forName("org.odk.collect.mapbox.MapboxMapConfigurator").newInstance() as MapConfigurator
        } catch (e: ClassNotFoundException) {
            null
        } catch (e: IllegalAccessException) {
            null
        } catch (e: InstantiationException) {
            null
        }
    }
}
