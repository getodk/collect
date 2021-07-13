/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.location.activities;

import android.content.Intent;

import org.junit.Before;
import org.odk.collect.android.location.client.FakeLocationClient;
import org.odk.collect.location.LocationClientProvider;
import org.odk.collect.android.geo.GoogleMapFragment;
import org.odk.collect.android.geo.MapboxMapFragment;
import org.robolectric.shadows.ShadowApplication;

public abstract class BaseGeoActivityTest {
    protected FakeLocationClient fakeLocationClient;
    protected final Intent intent = new Intent();

    @Before public void setUp() throws Exception {
        ShadowApplication.getInstance().grantPermissions("android.permission.ACCESS_FINE_LOCATION");
        ShadowApplication.getInstance().grantPermissions("android.permission.ACCESS_COARSE_LOCATION");
        GoogleMapFragment.testMode = true;
        MapboxMapFragment.testMode = true;
        fakeLocationClient = new FakeLocationClient();
        LocationClientProvider.setTestClient(fakeLocationClient);
    }
}
