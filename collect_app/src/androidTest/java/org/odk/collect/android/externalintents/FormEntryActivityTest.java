package org.odk.collect.android.externalintents;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.FormEntryActivity;

import java.io.IOException;

import static org.odk.collect.android.externalintents.ExportedActivitiesUtils.testDirectories;

@RunWith(AndroidJUnit4.class)
public class FormEntryActivityTest {

    @Rule
    public ActivityTestRule<FormEntryActivity> mFormEntryActivityRule =
            new ExportedActivityTestRule<>(FormEntryActivity.class);

    @Test
    public void formEntryActivityMakesDirsTest() throws IOException {
        testDirectories();
    }

}
