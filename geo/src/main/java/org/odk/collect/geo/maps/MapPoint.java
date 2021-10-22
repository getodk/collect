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

package org.odk.collect.geo.maps;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

public class MapPoint implements Parcelable {
    public final double lat;
    public final double lon;
    public final double alt;

    /**
     * This field contains the value that is called "accuracy" in Android APIs,
     * but the name "accuracy" is confusing because a higher "accuracy" value
     * actually represents lower accuracy.  What does an "accuracy" of 6 even mean?
     * In the Android documentation, this value is specified to be the radius,
     * in meters, of a 68% confidence circle (i.e. one standard deviation from
     * the mean); thus we use the more precise name "sd" for "standard deviation".
     */
    public final double sd;  // standard deviation in meters (68% confidence radius)

    public MapPoint(double lat, double lon) {
        this(lat, lon, 0, 0);
    }

    public MapPoint(double lat, double lon, double alt) {
        this(lat, lon, alt, 0);
    }

    public MapPoint(double lat, double lon, double alt, double sd) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.sd = sd;
    }

    private MapPoint(Parcel parcel) {
        this.lat = parcel.readDouble();
        this.lon = parcel.readDouble();
        this.alt = parcel.readDouble();
        this.sd = parcel.readDouble();
    }

    @Override public String toString() {
        return String.format(Locale.US, "MapPoint(%+.6f, %+.6f, %.0f, %.0f)", lat, lon, alt, sd);
    }

    @Override public int hashCode() {
        int result = Double.valueOf(lat).hashCode();
        result = result * 31 + Double.valueOf(lon).hashCode();
        result = result * 31 + Double.valueOf(alt).hashCode();
        result = result * 31 + Double.valueOf(sd).hashCode();
        return result;
    }

    @Override public boolean equals(Object other) {
        return other == this || (
            other instanceof MapPoint &&
            ((MapPoint) other).lat == this.lat &&
            ((MapPoint) other).lon == this.lon &&
            ((MapPoint) other).alt == this.alt &&
            ((MapPoint) other).sd == this.sd
        );
    }

    // Implementation of the Parcelable interface.

    public static final Parcelable.Creator<MapPoint> CREATOR = new Parcelable.Creator<MapPoint>() {
        public MapPoint createFromParcel(Parcel parcel) {
            return new MapPoint(parcel);
        }

        public MapPoint[] newArray(int size) {
            return new MapPoint[size];
        }
    };

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeDouble(lat);
        parcel.writeDouble(lon);
        parcel.writeDouble(alt);
        parcel.writeDouble(sd);
    }

}
