package org.odk.collect.android.externalintents;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.activities.SplashScreenActivity;

import java.io.IOException;

import static org.odk.collect.android.externalintents.ExportedActivitiesUtils.testDirectories;

public class SplashScreenActivityTest {

    @Rule
    public ActivityTestRule<SplashScreenActivity> mSplashScreenActivityRule =
            new ExportedActivityTestRule<>(SplashScreenActivity.class);

    @Test
    public void splashScreenActivityMakesDirsTest() throws IOException {
        testDirectories();
    }

}
