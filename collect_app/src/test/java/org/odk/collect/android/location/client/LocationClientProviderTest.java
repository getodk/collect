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

package org.odk.collect.android.location.client;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.odk.collect.android.utilities.PlayServicesChecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocationClientProviderTest {
    @Mock
    private Context context;

    @Mock
    private PlayServicesChecker playServicesChecker;

    @Mock
    private GoogleFusedLocationClient googleFusedLocationClient;

    @Test
    public void fusedLocationClient_returnedWhenPlayServicesAvailable() {
        when(playServicesChecker.isGooglePlayServicesAvailable(any())).thenReturn(true);

        LocationClient client = LocationClientProvider.getClient(context, playServicesChecker, () -> googleFusedLocationClient);
        assertThat(client, is(googleFusedLocationClient));
    }

    @Test
    public void androidLocationClient_returnedWhenPlayServicesNotAvailable() {
        when(playServicesChecker.isGooglePlayServicesAvailable(any())).thenReturn(false);

        LocationClient client = LocationClientProvider.getClient(context, playServicesChecker, () -> googleFusedLocationClient);
        assertThat(client, instanceOf(AndroidLocationClient.class));
    }
}
