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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.activities.GeoShapeActivity;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.map.GoogleMapFragment;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.location.activities.GeoPointActivityTest.newMockLocation;

@RunWith(RobolectricTestRunner.class)
public class GeoShapeActivityTest extends BaseGeoActivityTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();
    private ActivityController<GeoShapeActivity> controller;
    private GeoShapeActivity activity;
    private GoogleMapFragment map;
    @Mock LocationClient locationClient;

    @Before public void setUp() throws Exception {
        super.setUp();
        LocationClients.setTestClient(locationClient);
        GoogleMapFragment.testMode = true;
        controller = Robolectric.buildActivity(GeoShapeActivity.class);
        activity = controller.create().start().resume().visible().get();
        map = (GoogleMapFragment) activity.getMapFragment();
    }

    @Test public void activityShouldShowZoomDialogOnFirstLocation() {
        verify(locationClient).start();

        when(locationClient.isLocationAvailable()).thenReturn(true);
        map.onClientStart();
        verify(locationClient).requestLocationUpdates(map);

        assertFalse(activity.isGpsButtonEnabled());
        assertFalse(activity.isZoomDialogShowing());
        map.onLocationChanged(newMockLocation());
        assertTrue(activity.isGpsButtonEnabled());
        assertTrue(activity.isZoomDialogShowing());

        controller.stop();
        verify(locationClient).stop();
    }

    @Test public void activityShouldShowErrorDialogOnClientError() {
        assertFalse(map.isGpsErrorDialogShowing());
        map.onClientStartFailure();
        assertTrue(map.isGpsErrorDialogShowing());
    }

    @Test public void activityShouldShowErrorDialogIfLocationUnavailable() {
        assertFalse(map.isGpsErrorDialogShowing());
        when(locationClient.isLocationAvailable()).thenReturn(false);
        map.onClientStart();
        assertTrue(map.isGpsErrorDialogShowing());
    }
}
