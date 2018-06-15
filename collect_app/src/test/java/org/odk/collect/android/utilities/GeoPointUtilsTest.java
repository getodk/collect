package org.odk.collect.android.utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GeoPointUtilsTest {
    @Test
    public void capitalizesGps() {
        String input = "gps";
        assertEquals("GPS", GeoPointUtils.capitalizeGps(input));

        String locationProvider = "network";
        assertEquals("network", GeoPointUtils.capitalizeGps(locationProvider));

        String nullLocationProvider = null;
        assertNull(GeoPointUtils.capitalizeGps(nullLocationProvider));
    }
}
