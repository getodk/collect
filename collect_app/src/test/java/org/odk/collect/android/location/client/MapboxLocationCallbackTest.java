package org.odk.collect.android.location.client;

import android.location.Location;

import com.mapbox.android.core.location.LocationEngineResult;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.location.LocationTestUtils;

import static android.location.LocationManager.GPS_PROVIDER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MapboxLocationCallbackTest {
    private MapboxLocationCallback mapboxLocationCallback;
    private TestLocationListener locationListener;
    private final LocationEngineResult result = mock(LocationEngineResult.class);

    @Before
    public void setup() {
        locationListener = spy(new TestLocationListener());
        mapboxLocationCallback = new MapboxLocationCallback(locationListener);
    }

    @Test
    public void whenLocationIsNull_shouldNotBePassedToListener() {
        when(result.getLastLocation()).thenReturn(null);
        mapboxLocationCallback.onSuccess(result);

        verify(locationListener, never()).onLocationChanged(null);
        assertThat(locationListener.getLastLocation(), is(nullValue()));
    }

    @Test
    public void whenLocationIsNotNull_shouldBePassedToListener() {
        Location location = LocationTestUtils.createLocation(GPS_PROVIDER, 7d, 2d, 3d, 5.0f);
        when(result.getLastLocation()).thenReturn(location);
        mapboxLocationCallback.onSuccess(result);

        Location receivedLocation = locationListener.getLastLocation();
        assertThat(location, is(receivedLocation));
    }

    @Test
    public void whenAccuracyIsNegative_shouldBeSanitized() {
        Location location = LocationTestUtils.createLocation(GPS_PROVIDER, 7d, 2d, 3d, -1.0f);
        when(result.getLastLocation()).thenReturn(location);
        mapboxLocationCallback.onSuccess(result);

        assertThat(locationListener.getLastLocation().getAccuracy(), is(0.0f));
    }

    @Test
    public void whenLocationIsFaked_shouldAccuracyBeSetToZero() {
        Location location = LocationTestUtils.createLocation(GPS_PROVIDER, 7d, 2d, 3d, 5.0f, true);
        when(result.getLastLocation()).thenReturn(location);
        mapboxLocationCallback.onSuccess(result);

        assertThat(locationListener.getLastLocation().getAccuracy(), is(0.0f));
    }
}
