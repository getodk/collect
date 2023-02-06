/*
 * Copyright (C) 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.maps

import android.os.Parcel
import android.os.Parcelable
import java.util.Locale

class MapPoint : Parcelable {

    @JvmField
    val latitude: Double

    @JvmField
    val longitude: Double

    @JvmField
    val altitude: Double

    /**
     * This field contains the value that is called "accuracy" in Android APIs,
     * but the name "accuracy" is confusing because a higher "accuracy" value
     * actually represents lower accuracy.  What does an "accuracy" of 6 even mean?
     * In the Android documentation, this value is specified to be the radius,
     * in meters, of a 68% confidence circle (i.e. one standard deviation from
     * the mean); thus we use the more precise name "sd" for "standard deviation".
     */
    @JvmField
    val sd: Double // standard deviation in meters (68% confidence radius)

    @JvmOverloads
    constructor(latitude: Double, longitude: Double, altitude: Double = 0.0, sd: Double = 0.0) {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.sd = sd
    }

    private constructor(parcel: Parcel) {
        latitude = parcel.readDouble()
        longitude = parcel.readDouble()
        altitude = parcel.readDouble()
        sd = parcel.readDouble()
    }

    override fun toString(): String {
        return String.format(
            Locale.US, "MapPoint(%+.6f, %+.6f, %.0f, %.0f)", latitude, longitude, altitude, sd
        )
    }

    override fun hashCode(): Int {
        var result = java.lang.Double.valueOf(latitude).hashCode()
        result = result * 31 + java.lang.Double.valueOf(longitude).hashCode()
        result = result * 31 + java.lang.Double.valueOf(altitude).hashCode()
        result = result * 31 + java.lang.Double.valueOf(sd).hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        return other === this || other is MapPoint && other.latitude == latitude && (other as MapPoint).longitude == longitude && (other as MapPoint).altitude == altitude && (other as MapPoint).sd == sd
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeDouble(altitude)
        parcel.writeDouble(sd)
    }

    companion object {
        // Implementation of the Parcelable interface.
        @JvmField
        val CREATOR: Parcelable.Creator<MapPoint> = object : Parcelable.Creator<MapPoint> {
            override fun createFromParcel(parcel: Parcel): MapPoint? {
                return MapPoint(parcel)
            }

            override fun newArray(size: Int): Array<MapPoint?> {
                return arrayOfNulls(size)
            }
        }
    }
}
