package org.odk.collect.android.location.client;

import android.location.Location;

import com.mapbox.android.core.location.LocationEngineResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.odk.collect.android.geo.MapboxMapFragment;
import org.odk.collect.testshared.LocationTestUtils;

import static android.location.LocationManager.GPS_PROVIDER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MapboxLocationCallbackTest {
    private MapboxLocationCallback mapboxLocationCallback;
    private MapboxMapFragment mapFragment;
    private final LocationEngineResult result = mock(LocationEngineResult.class);

    @Before
    public void setup() {
        mapFragment = mock(MapboxMapFragment.class);
        mapboxLocationCallback = new MapboxLocationCallback(mapFragment);
    }

    @Test
    public void whenLocationIsNull_shouldNotBePassedToListener() {
        when(result.getLastLocation()).thenReturn(null);
        mapboxLocationCallback.onSuccess(result);

        verify(mapFragment, never()).onLocationChanged(null);
    }

    @Test
    public void whenLocationIsNotNull_shouldBePassedToListener() {
        Location location = LocationTestUtils.createLocation(GPS_PROVIDER, 7d, 2d, 3d, 5.0f);
        when(result.getLastLocation()).thenReturn(location);
        mapboxLocationCallback.onSuccess(result);

        verify(mapFragment).onLocationChanged(location);
    }

    @Test
    public void whenAccuracyIsNegative_shouldBeSanitized() {
        Location location = LocationTestUtils.createLocation(GPS_PROVIDER, 7d, 2d, 3d, -1.0f);
        when(result.getLastLocation()).thenReturn(location);
        mapboxLocationCallback.onSuccess(result);

        ArgumentCaptor<Location> acLocation = ArgumentCaptor.forClass(Location.class);
        verify(mapFragment).onLocationChanged(acLocation.capture());
        assertThat(acLocation.getValue().getAccuracy(), is(0.0f));

    }

    @Test
    public void whenLocationIsFaked_shouldAccuracyBeSetToZero() {
        Location location = LocationTestUtils.createLocation(GPS_PROVIDER, 7d, 2d, 3d, 5.0f, true);
        when(result.getLastLocation()).thenReturn(location);
        mapboxLocationCallback.onSuccess(result);

        ArgumentCaptor<Location> acLocation = ArgumentCaptor.forClass(Location.class);
        verify(mapFragment).onLocationChanged(acLocation.capture());
        assertThat(acLocation.getValue().getAccuracy(), is(0.0f));
    }
}
