package org.odk.collect.android.location;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.GeoPointActivity;

@RunWith(AndroidJUnit4.class)
public class GeoPointActivityTest {

    @Rule
    private ActivityTestRule<GeoPointActivity> activityTestRule = new ActivityTestRule<>(GeoPointActivity.class);

    private GeoPointActivity activity;

    @Before
    public void setUp() {
        activity = activityTestRule.getActivity();
    }

    @Test
    public void testLocationStartsCorrectly() {

    }
}
