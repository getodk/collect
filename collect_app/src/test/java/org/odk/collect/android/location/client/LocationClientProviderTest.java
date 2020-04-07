package org.odk.collect.android.location.client;

import android.content.Context;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.utilities.PlayServicesChecker;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class LocationClientProviderTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public Context context;

    @Mock
    public PlayServicesChecker playServicesChecker;

    @Test
    public void fusedLocationClient_returnedWhenPlayServicesAvailable() {
        when(playServicesChecker.isGooglePlayServicesAvailable(any())).thenReturn(true);

        // Could import Robolectric Shadows Play Services to actually build a GoogleFusedLocationClient
        // but it wouldn't tell us anything more useful.
        try {
            LocationClientProvider.getClient(context, playServicesChecker);
        } catch (Exception e) {
            assertThat(Log.getStackTraceString(e), containsString("GoogleFusedLocationClient"));
        }
    }

    @Test
    public void androidLocationClient_returnedWhenPlayServicesNotAvailable() {
        when(playServicesChecker.isGooglePlayServicesAvailable(any())).thenReturn(false);

        LocationClient client = LocationClientProvider.getClient(context, playServicesChecker);
        assertThat(client, instanceOf(AndroidLocationClient.class));
    }
}
