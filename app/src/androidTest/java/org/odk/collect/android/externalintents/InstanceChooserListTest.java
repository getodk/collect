package org.odk.collect.android.externalintents;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.InstanceChooserList;

import java.io.IOException;

import static org.odk.collect.android.externalintents.ImplicitActivitiesUtils.clearDirectories;
import static org.odk.collect.android.externalintents.ImplicitActivitiesUtils.testDirectories;

@RunWith(AndroidJUnit4.class)
public class InstanceChooserListTest {

    @Rule
    public ActivityTestRule<InstanceChooserList> mInstanceChooserListRule =
            new ActivityTestRule<>(InstanceChooserList.class, false, false);

    @Before
    public void setUp() {

        clearDirectories();

        Intent intent = new Intent();
        mInstanceChooserListRule.launchActivity(intent);

    }

    @Test
    public void instanceChooserListMakesDirsTest() throws IOException {
        testDirectories();
    }

}
