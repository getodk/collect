package org.odk.collect.android.location;

import android.location.Location;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class LocationTestUtils {
    private LocationTestUtils() {

    }

    public static Location createLocation(String provider, double lat, double lon, double alt) {
        Location location = spy(new Location(provider));
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(alt);
        return location;
    }

    public static Location createLocation(String provider, double lat, double lon, double alt, float sd) {
        Location location = createLocation(provider, lat, lon, alt);
        location.setAccuracy(sd);
        return location;
    }

    public static Location createLocation(String provider, double lat, double lon, double alt, float sd, boolean isLocationMocked) {
        Location location = createLocation(provider, lat, lon, alt, sd);
        when(location.isFromMockProvider()).thenReturn(isLocationMocked);
        return location;
    }
}
