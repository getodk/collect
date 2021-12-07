/*
 * Copyright (C) 2018 Nafundi
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

package org.odk.collect.android.location;

import static org.junit.Assert.assertEquals;
import static org.odk.collect.testshared.LocationTestUtils.createLocation;

import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.R;
import org.odk.collect.android.geo.GoogleMapFragment;
import org.odk.collect.android.geo.MapboxMapFragment;
import org.odk.collect.android.location.client.FakeLocationClient;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.geo.GeoPolyActivity;
import org.odk.collect.geo.GeoUtils;
import org.odk.collect.location.LocationClientProvider;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AndroidJUnit4.class)
public class GeoPolyActivityTestOld {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    private ActivityController<GeoPolyActivity> controller;
    private FakeLocationClient fakeLocationClient;

    @Before
    public void setUp() throws Exception {
        ShadowApplication.getInstance().grantPermissions("android.permission.ACCESS_FINE_LOCATION");
        ShadowApplication.getInstance().grantPermissions("android.permission.ACCESS_COARSE_LOCATION");
        GoogleMapFragment.testMode = true;
        MapboxMapFragment.testMode = true;
        fakeLocationClient = new FakeLocationClient();
        LocationClientProvider.setTestClient(fakeLocationClient);

        CollectHelpers.setupDemoProject();
        controller = Robolectric.buildActivity(GeoPolyActivity.class);
    }

    @Test
    //Cm accuracy #4198 +
    public void locationAccuracyIsFormattedInAppropriateUnit() {
        GeoPolyActivity activity = controller.create().start().resume().visible().get();
        TextView locationStatus = activity.findViewById(R.id.location_status);
        for (double accuracy : GeoUtils.TEST_ACCURACIES) {
            fakeLocationClient.receiveFix(
                    createLocation("GPS", 11, 12, 13, (float) accuracy));
            boolean useCm = accuracy < 1;
            String expected = activity.getString(
                    useCm ? R.string.location_status_accuracy_cm : R.string.location_status_accuracy,
                    (float) (accuracy * (useCm ? 100 : 1))
            );
            assertEquals(expected, locationStatus.getText());
        }
    }
}
