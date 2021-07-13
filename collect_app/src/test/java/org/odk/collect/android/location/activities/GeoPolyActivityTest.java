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

import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.GeoPolyActivity;
import org.odk.collect.android.geo.MapPoint;
import org.odk.collect.android.support.CollectHelpers;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.testshared.LocationTestUtils.createLocation;

@RunWith(AndroidJUnit4.class)
public class GeoPolyActivityTest extends BaseGeoActivityTest {
    
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    private ActivityController<GeoPolyActivity> controller;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        CollectHelpers.setupDemoProject();
        controller = Robolectric.buildActivity(GeoPolyActivity.class, intent);
    }

    @Test
    public void testLocationClientLifecycle() {
        // Starting the activity should start the location client.
        GeoPolyActivity activity = controller.create().start().resume().visible().get();
        assertTrue(fakeLocationClient.isRunning());

        // Acquiring a fix should set the location on the map.
        fakeLocationClient.receiveFix(createLocation("GPS", 11, 12, 13, 14f));
        assertEquals(new MapPoint(11, 12, 13, 14), activity.getMapFragment().getGpsLocation());

        // Acquiring a second fix should update the map with the new location.
        fakeLocationClient.receiveFix(createLocation("GPS", 21, 22, 23, 24f));
        assertEquals(new MapPoint(21, 22, 23, 24), activity.getMapFragment().getGpsLocation());

        // Stopping the activity should stop the location client.
        controller.stop();
        assertFalse(fakeLocationClient.isRunning());
    }

    @Test
    public void recordButton_should_beHiddenForAutomaticMode() {
        GeoPolyActivity activity = controller.create().start().resume().visible().get();
        activity.updateRecordingMode(R.id.automatic_mode);
        activity.startInput();
        assertThat(activity.findViewById(R.id.record_button).getVisibility(), is(View.GONE));
    }

    @Test
    public void recordButton_should_beVisibleForManualMode() {
        GeoPolyActivity activity = controller.create().start().resume().visible().get();
        activity.updateRecordingMode(R.id.manual_mode);
        activity.startInput();
        assertThat(activity.findViewById(R.id.record_button).getVisibility(), is(View.VISIBLE));
    }
}
