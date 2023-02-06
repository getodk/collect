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

data class MapPoint @JvmOverloads constructor(
    @JvmField val latitude: Double,
    @JvmField val longitude: Double,
    @JvmField val altitude: Double = 0.0,
    @JvmField val sd: Double = 0.0
) : Parcelable {

    private constructor(parcel: Parcel) : this(parcel.readDouble(), parcel.readDouble(), parcel.readDouble(), parcel.readDouble())

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
