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

import android.location.Location;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.activities.GeoTraceActivity;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.client.LocationClients;
import org.odk.collect.android.map.GoogleMapFragment;
import org.odk.collect.android.map.MapPoint;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.location.activities.GeoPointActivityTest.newMockLocation;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class GeoTraceActivityTest extends BaseGeoActivityTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();
    private ActivityController<GeoTraceActivity> controller;
    private GeoTraceActivity activity;
    private GoogleMapFragment map;
    @Mock LocationClient locationClient;

    @Before public void setUp() throws Exception {
        super.setUp();
        LocationClients.setTestClient(locationClient);
        GoogleMapFragment.testMode = true;
        controller = Robolectric.buildActivity(GeoTraceActivity.class);
        activity = controller.create().start().resume().visible().get();
        map = (GoogleMapFragment) activity.getMapFragment();
    }

    @Test public void testLocationClientLifecycle() {
        verify(locationClient).start();

        when(locationClient.isLocationAvailable()).thenReturn(true);

        Location mockLocation = newMockLocation();
        when(mockLocation.getLatitude()).thenReturn(11.0);
        when(mockLocation.getLongitude()).thenReturn(12.0);
        when(mockLocation.getAltitude()).thenReturn(13.0);
        when(mockLocation.getAccuracy()).thenReturn(14f);
        when(locationClient.getLastLocation()).thenReturn(mockLocation);

        map.onClientStart();
        verify(locationClient).requestLocationUpdates(map);
        verify(locationClient).getLastLocation();

        MapPoint point = map.getGpsLocation();
        assertEquals(mockLocation.getLatitude(), point.lat, 0);
        assertEquals(mockLocation.getLongitude(), point.lon, 0);
        assertEquals(mockLocation.getAltitude(), point.alt, 0);
        assertEquals(mockLocation.getAccuracy(), point.sd, 0);

        Location mockLocation2 = newMockLocation();
        when(mockLocation2.getLatitude()).thenReturn(21.0);
        when(mockLocation2.getLongitude()).thenReturn(22.0);
        when(mockLocation2.getAltitude()).thenReturn(23.0);
        when(mockLocation2.getAccuracy()).thenReturn(24f);

        activity.getPlayButton().setEnabled(false);
        map.onLocationChanged(mockLocation2);
        assertTrue(activity.getPlayButton().isEnabled());
        MapPoint point2 = map.getGpsLocation();
        assertEquals(mockLocation2.getLatitude(), point2.lat, 0);
        assertEquals(mockLocation2.getLongitude(), point2.lon, 0);
        assertEquals(mockLocation2.getAltitude(), point2.alt, 0);
        assertEquals(mockLocation2.getAccuracy(), point2.sd, 0);

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
