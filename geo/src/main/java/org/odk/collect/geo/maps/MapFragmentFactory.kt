package org.odk.collect.geo.maps

import android.content.Context

interface MapFragmentFactory {
    fun createMapFragment(context: Context): MapFragment?
}
