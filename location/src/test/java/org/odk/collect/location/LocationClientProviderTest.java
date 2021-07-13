/*
 * Copyright (C) 2020 Nafundi
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

package org.odk.collect.location;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocationClientProviderTest {

    private final Context context = mock(Context.class);
    private final GoogleApiAvailability googleApiAvailability = mock(GoogleApiAvailability.class);
    private final GoogleFusedLocationClient googleFusedLocationClient = mock(GoogleFusedLocationClient.class);

    @Test
    public void fusedLocationClient_returnedWhenPlayServicesAvailable() {
        when(googleApiAvailability.isGooglePlayServicesAvailable(any())).thenReturn(ConnectionResult.SUCCESS);

        LocationClient client = LocationClientProvider.getClient(context, () -> googleFusedLocationClient, googleApiAvailability);
        MatcherAssert.assertThat(client, is(googleFusedLocationClient));
    }

    @Test
    public void androidLocationClient_returnedWhenPlayServicesNotAvailable() {
        when(googleApiAvailability.isGooglePlayServicesAvailable(any())).thenReturn(ConnectionResult.API_UNAVAILABLE);

        LocationClient client = LocationClientProvider.getClient(context, () -> googleFusedLocationClient, googleApiAvailability);
        MatcherAssert.assertThat(client, instanceOf(AndroidLocationClient.class));
    }
}
