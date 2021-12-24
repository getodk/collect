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
package org.odk.collect.location

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class LocationClientProviderTest {

    private val context = Mockito.mock(Context::class.java)
    private val googleApiAvailability = Mockito.mock(
        GoogleApiAvailability::class.java
    )
    private val googleFusedLocationClient = Mockito.mock(
        GoogleFusedLocationClient::class.java
    )

    @Test
    fun fusedLocationClient_returnedWhenPlayServicesAvailable() {
        `when`(googleApiAvailability.isGooglePlayServicesAvailable(ArgumentMatchers.any()))
            .thenReturn(
                ConnectionResult.SUCCESS
            )
        val client = LocationClientProvider.getClient(
            context,
            { googleFusedLocationClient },
            googleApiAvailability
        )

        assertThat(client, `is`(googleFusedLocationClient))
    }

    @Test
    fun androidLocationClient_returnedWhenPlayServicesNotAvailable() {
        `when`(googleApiAvailability.isGooglePlayServicesAvailable(ArgumentMatchers.any()))
            .thenReturn(
                ConnectionResult.API_UNAVAILABLE
            )

        val client = LocationClientProvider.getClient(
            context,
            { googleFusedLocationClient },
            googleApiAvailability
        )

        assertThat(client, instanceOf(AndroidLocationClient::class.java))
    }
}
