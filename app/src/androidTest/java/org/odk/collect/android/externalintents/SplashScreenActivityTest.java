package org.odk.collect.android.externalintents;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.activities.SplashScreenActivity;

import java.io.IOException;

import static org.odk.collect.android.externalintents.ImplicitActivitiesUtils.clearDirectories;
import static org.odk.collect.android.externalintents.ImplicitActivitiesUtils.testDirectories;

public class SplashScreenActivityTest {

    @Rule
    public ActivityTestRule<SplashScreenActivity> mSplashScreenActivityRule =
            new ActivityTestRule<>(SplashScreenActivity.class, false, false);

    @Before
    public void setUp() {

        clearDirectories();

        Intent intent = new Intent();
        mSplashScreenActivityRule.launchActivity(intent);

    }

    @Test
    public void splashScreenActivityMakesDirsTest() throws IOException {

        testDirectories();

    }

}
