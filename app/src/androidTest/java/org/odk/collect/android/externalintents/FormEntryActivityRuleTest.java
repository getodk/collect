package org.odk.collect.android.externalintents;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.FormEntryActivity;

import java.io.IOException;

import static org.odk.collect.android.externalintents.ImplicitActivitiesUtils.clearDirectories;
import static org.odk.collect.android.externalintents.ImplicitActivitiesUtils.testDirectories;

@RunWith(AndroidJUnit4.class)
public class FormEntryActivityRuleTest {

    @Rule
    public ActivityTestRule<FormEntryActivity> mFormEntryActivityRule =
            new ActivityTestRule<>(FormEntryActivity.class, false, false);

    @Before
    public void setUp() {

        clearDirectories();

        Intent intent = new Intent();
        mFormEntryActivityRule.launchActivity(intent);

    }

    @Test
    public void formEntryActivityMakesDirsTest() throws IOException {

        testDirectories();

    }

}
