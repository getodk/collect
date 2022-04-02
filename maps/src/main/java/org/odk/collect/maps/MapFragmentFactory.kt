package org.odk.collect.maps

import android.content.Context

interface MapFragmentFactory {
    fun createMapFragment(context: Context): MapFragment?
}
