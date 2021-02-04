package org.odk.collect.android.utilities;

import android.location.Location;
import android.os.Bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.location.LocationTestUtils;
import org.odk.collect.android.storage.StoragePathProvider;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

import static android.location.LocationManager.GPS_PROVIDER;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class GeoUtilsTest {
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
    public void whenPathIsNull_should_getReferenceLayerFileReturnNull() {
        Bundle config = mock(Bundle.class);
        StoragePathProvider storagePathProvider = mock(StoragePathProvider.class);
        when(storagePathProvider.getAbsoluteOfflineMapLayerPath(any())).thenReturn(null);

        assertNull(GeoUtils.getReferenceLayerFile(config, storagePathProvider));
    }

    @Test
    public void whenOfflineLayerFileDoesNotExist_should_getReferenceLayerFileReturnNull() {
        Bundle config = mock(Bundle.class);
        StoragePathProvider storagePathProvider = mock(StoragePathProvider.class);
        when(storagePathProvider.getAbsoluteOfflineMapLayerPath(any())).thenReturn("/storage/emulated/0/odk/layers/MapBox_Demo_Layer/demo_layers.mbtiles");

        assertNull(GeoUtils.getReferenceLayerFile(config, storagePathProvider));
    }

    @Test
    public void whenOfflineLayerFileExist_should_getReferenceLayerFileReturnThatFile() {
        File file = new File(new StoragePathProvider().getAbsoluteOfflineMapLayerPath("MapBox_Demo_Layer/demo_layers.mbtiles"));
        FileUtils.write(file, new byte[] {});

        Bundle config = mock(Bundle.class);
        StoragePathProvider storagePathProvider = mock(StoragePathProvider.class);
        when(storagePathProvider.getAbsoluteOfflineMapLayerPath(any())).thenReturn(file.getAbsolutePath());

        assertNotNull(GeoUtils.getReferenceLayerFile(config, storagePathProvider));
    }

    @Test
    public void whenAccuracyIsNegative_shouldBeSetToZeroAfterSanitizing() {
        Location location = LocationTestUtils.createLocation(GPS_PROVIDER, 7, 2, 3, -1.0f);
        Location sanitizedLocation = GeoUtils.sanitizeAccuracy(location);
        assertThat(sanitizedLocation.getLatitude(), is(7.0));
        assertThat(sanitizedLocation.getLongitude(), is(2.0));
        assertThat(sanitizedLocation.getAltitude(), is(3.0));
        assertThat(sanitizedLocation.getAccuracy(), is(0.0f));
    }

    @Test
    public void whenLocationIsMocked_shouldAccuracyBeSetToZeroAfterSanitizing() {
        Location location = LocationTestUtils.createLocation(GPS_PROVIDER, 7, 2, 3, 5.0f, true);
        Location sanitizedLocation = GeoUtils.sanitizeAccuracy(location);
        assertThat(sanitizedLocation.getLatitude(), is(7.0));
        assertThat(sanitizedLocation.getLongitude(), is(2.0));
        assertThat(sanitizedLocation.getAltitude(), is(3.0));
        assertThat(sanitizedLocation.getAccuracy(), is(0.0f));
    }

    @Test
    public void whenLocationIsNull_shouldNullBeReturnedAfterSanitizing() {
        assertThat(GeoUtils.sanitizeAccuracy(null), nullValue());
    }
}
