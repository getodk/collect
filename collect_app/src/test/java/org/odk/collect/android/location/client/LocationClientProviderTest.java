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
