package org.odk.collect.android.utilities;

import android.location.Location;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.geo.MapPoint;
import org.odk.collect.shared.TempFiles;
import org.odk.collect.testshared.LocationTestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.odk.collect.android.geo.MapFragment.KEY_REFERENCE_LAYER;

@RunWith(AndroidJUnit4.class)
public class GeoUtilsTest {
    private final List<MapPoint> points = new ArrayList<>(Arrays.asList(
            new MapPoint(11, 12, 13, 14),
            new MapPoint(21, 22, 23, 24),
            new MapPoint(31, 32, 33, 34)
    ));

    @Test
    public void whenPointsAreNull_formatPoints_returnsEmptyString() {
        assertEquals(GeoUtils.formatPointsResultString(Collections.emptyList(), true), "");
        assertEquals(GeoUtils.formatPointsResultString(Collections.emptyList(), false), "");
    }

    @Test
    public void geotraces_areSeparatedBySemicolon_withoutTrialingSemicolon() {
        assertEquals(GeoUtils.formatPointsResultString(points, false),
                "11.0 12.0 13.0 14.0;21.0 22.0 23.0 24.0;31.0 32.0 33.0 34.0");
    }

    @Test
    public void geoshapes_areSeparatedBySemicolon_withoutTrialingSemicolon_andHaveMatchingFirstAndLastPoints() {
        assertEquals(GeoUtils.formatPointsResultString(points, true),
                "11.0 12.0 13.0 14.0;21.0 22.0 23.0 24.0;31.0 32.0 33.0 34.0;11.0 12.0 13.0 14.0");
    }

    @Test
    public void test_formatLocationResultString() {
        Location location = LocationTestUtils.createLocation("GPS", 1, 2, 3, 4);
        assertEquals(GeoUtils.formatLocationResultString(location), "1.0 2.0 3.0 4.0");
    }

    @Test
    public void capitalizesGps() {
        String input = "gps";
        assertEquals("GPS", GeoUtils.capitalizeGps(input));

        String locationProvider = "network";
        assertEquals("network", GeoUtils.capitalizeGps(locationProvider));

        String nullLocationProvider = null;
        assertNull(GeoUtils.capitalizeGps(nullLocationProvider));
    }

    @Test
    public void getReferenceLayerFile_whenPathIsNull_should_getReferenceLayerFileReturnNull() {
        String layersPath = TempFiles.createTempDir().getAbsolutePath();
        Bundle config = new Bundle();
        config.putString(KEY_REFERENCE_LAYER, null);

        assertNull(GeoUtils.getReferenceLayerFile(config, layersPath));
    }

    @Test
    public void whenOfflineLayerFileDoesNotExist_should_getReferenceLayerFileReturnNull() {
        String layersPath = TempFiles.createTempDir().getAbsolutePath();
        Bundle config = new Bundle();
        config.putString(KEY_REFERENCE_LAYER, "blah");

        assertNull(GeoUtils.getReferenceLayerFile(config, layersPath));
    }

    @Test
    public void whenOfflineLayerFileExist_should_getReferenceLayerFileReturnThatFile() {
        String layersPath = TempFiles.createTempDir().getAbsolutePath();
        FileUtils.write(new File(layersPath, "blah"), new byte[]{});

        Bundle config = new Bundle();
        config.putString(KEY_REFERENCE_LAYER, "blah");

        assertNotNull(GeoUtils.getReferenceLayerFile(config, layersPath));
    }
}
