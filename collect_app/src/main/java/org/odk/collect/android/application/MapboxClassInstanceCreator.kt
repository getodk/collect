package org.odk.collect.android.application

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
}
