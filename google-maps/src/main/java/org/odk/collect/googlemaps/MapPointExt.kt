package org.odk.collect.googlemaps

import com.google.android.gms.maps.model.LatLng
import org.odk.collect.maps.MapPoint

object MapPointExt {
    @JvmStatic
    fun MapPoint.toLatLng(): LatLng {
        return LatLng(this.latitude, this.longitude)
    }
}