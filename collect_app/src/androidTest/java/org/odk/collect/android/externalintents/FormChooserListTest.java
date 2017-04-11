package org.odk.collect.android.externalintents;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.activities.FormChooserList;

import java.io.IOException;

import static org.odk.collect.android.externalintents.ExportedActivitiesUtils.testDirectories;

public class FormChooserListTest {

    @Rule
    public ActivityTestRule<FormChooserList> mFormChooserListRule =
            new ExportedActivityTestRule<>(FormChooserList.class);

    @Test
    public void formChooserListMakesDirsTest() throws IOException {
        testDirectories();
    }

}
