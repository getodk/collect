package org.odk.collect.android.externalintents;

import android.support.test.filters.Suppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.InstanceChooserList;

import java.io.IOException;

import static org.odk.collect.android.externalintents.ExportedActivitiesUtils.testDirectories;

@Suppress
// Frequent failures: https://github.com/opendatakit/collect/issues/796
@RunWith(AndroidJUnit4.class)
public class InstanceChooserListTest {

    @Rule
    public ActivityTestRule<InstanceChooserList> instanceChooserListRule =
            new ExportedActivityTestRule<>(InstanceChooserList.class);

    @Test
    public void instanceChooserListMakesDirsTest() throws IOException {
        testDirectories();
    }

}
