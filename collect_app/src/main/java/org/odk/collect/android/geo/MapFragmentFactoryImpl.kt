package org.odk.collect.android.geo

import android.content.Context
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapFragmentFactory

class MapFragmentFactoryImpl : MapFragmentFactory {

    override fun createMapFragment(context: Context): MapFragment? {
        return OsmDroidMapFragment()
    }
}
