package org.odk.collect.android.geo

import android.content.Context

interface MapFragmentFactory {
    fun createMapFragment(context: Context): MapFragment?
}
