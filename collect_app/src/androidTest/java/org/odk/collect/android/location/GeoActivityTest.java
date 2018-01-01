package org.odk.collect.android.location;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class GeoActivityTest {

    @Rule
    public ActivityTestRule<GeoActivity> activityTestRule;

    @Test
    public void shouldDoSomething() {
        assertTrue(true);
    }
}
