package org.odk.collect.android.externalintents;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.activities.MainMenuActivity;

import java.io.IOException;

import static org.odk.collect.android.externalintents.ExportedActivitiesUtils.testDirectories;

public class MainMenuActivityTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> mMainMenuActivityRule =
            new ExportedActivityTestRule<>(MainMenuActivity.class);

    @Test
    public void mainMenuActivityMakesDirsTest() throws IOException {
        testDirectories();
    }

}
