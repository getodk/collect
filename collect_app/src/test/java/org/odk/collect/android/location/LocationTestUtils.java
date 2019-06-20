package org.odk.collect.android.location;

import android.location.Location;

public class LocationTestUtils {
    private LocationTestUtils() {

    }

    public static Location createLocation(String provider, double lat, double lon, double alt, float sd) {
        Location location = new Location(provider);
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(alt);
        location.setAccuracy(sd);
        return location;
    }

    public static Location createLocation(String provider, double lat, double lon, double alt) {
        Location location = new Location(provider);
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(alt);
        return location;
    }
}
