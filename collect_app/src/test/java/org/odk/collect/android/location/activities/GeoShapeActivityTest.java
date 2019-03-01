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

package org.odk.collect.android.location.activities;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.activities.GeoShapeActivity;
import org.odk.collect.android.location.client.FakeLocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.map.GoogleMapFragment;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class GeoShapeActivityTest extends BaseGeoActivityTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();
    private ActivityController<GeoShapeActivity> controller;
    private GeoShapeActivity activity;
    private FakeLocationClient fakeLocationClient;

    @Before public void setUp() throws Exception {
        super.setUp();
        fakeLocationClient = new FakeLocationClient();
        LocationClients.setTestClient(fakeLocationClient);
        GoogleMapFragment.testMode = true;
        controller = Robolectric.buildActivity(GeoShapeActivity.class);
    }

    @Test public void shouldEnableZoomButtonOnFirstLocationFix() {
        // Starting the activity should start the location client.
        activity = controller.create().start().resume().visible().get();
        assertTrue(fakeLocationClient.isRunning());

        // Initially, the location button should be disabled.
        assertFalse(activity.isZoomButtonEnabled());

        // A location fix should enable the location button.
        fakeLocationClient.receiveFix(createLocation("GPS", 1, 2, 3, 4f));
        assertTrue(activity.isZoomButtonEnabled());

        // Stopping the activity should stop the location client.
        controller.stop();
        assertFalse(fakeLocationClient.isRunning());
    }

    @Test public void shouldShowErrorDialogIfLocationClientFails() {
        fakeLocationClient.setFailOnStart(true);
        activity = controller.create().start().resume().visible().get();
        assertTrue(((GoogleMapFragment) activity.getMapFragment()).isGpsErrorDialogShowing());
    }

    @Test public void shouldShowErrorDialogIfLocationUnavailable() {
        fakeLocationClient.setLocationAvailable(false);
        activity = controller.create().start().resume().visible().get();
        assertTrue(((GoogleMapFragment) activity.getMapFragment()).isGpsErrorDialogShowing());
    }
}
