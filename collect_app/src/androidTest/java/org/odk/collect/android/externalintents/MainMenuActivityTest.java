package org.odk.collect.android.externalintents;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.activities.MainMenuActivity;

import java.io.IOException;

import static org.odk.collect.android.externalintents.ImplicitActivitiesUtils.clearDirectories;
import static org.odk.collect.android.externalintents.ImplicitActivitiesUtils.testDirectories;

public class MainMenuActivityTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> mMainMenuActivityRule =
            new ActivityTestRule<>(MainMenuActivity.class, false, false);

    @Before
    public void setUp() {

        clearDirectories();

        Intent intent = new Intent();
        mMainMenuActivityRule.launchActivity(intent);

    }

    @Test
    public void mainMenuActivityMakesDirsTest() throws IOException {

        testDirectories();

    }

}
