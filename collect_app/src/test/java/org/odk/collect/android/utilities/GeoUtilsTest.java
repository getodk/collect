package org.odk.collect.android.utilities;

import android.os.Bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.storage.StoragePathProvider;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

import static junit.framework.TestCase.assertNotNull;
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
}
