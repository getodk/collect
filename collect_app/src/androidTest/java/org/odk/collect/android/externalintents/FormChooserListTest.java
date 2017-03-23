package org.odk.collect.android.externalintents;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.activities.FormChooserList;

import java.io.IOException;

import static org.odk.collect.android.externalintents.ImplicitActivitiesUtils.clearDirectories;
import static org.odk.collect.android.externalintents.ImplicitActivitiesUtils.testDirectories;

public class FormChooserListTest {

    @Rule
    public ActivityTestRule<FormChooserList> mFormChooserListRule =
            new ActivityTestRule<>(FormChooserList.class, false, false);

    @Before
    public void setUp() {

        clearDirectories();

        Intent intent = new Intent();
        mFormChooserListRule.launchActivity(intent);

    }

    @Test
    public void formChooserListMakesDirsTest() throws IOException {

        testDirectories();

    }

}
