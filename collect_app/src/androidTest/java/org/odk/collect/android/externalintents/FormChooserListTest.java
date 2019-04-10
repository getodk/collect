package org.odk.collect.android.externalintents;

import androidx.test.filters.Suppress;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.activities.FormChooserList;

import java.io.IOException;

import static org.odk.collect.android.externalintents.ExportedActivitiesUtils.testDirectories;

@Suppress
// Frequent failures: https://github.com/opendatakit/collect/issues/796
public class FormChooserListTest {

    @Rule
    public ActivityTestRule<FormChooserList> formChooserListRule =
            new ExportedActivityTestRule<>(FormChooserList.class);

    @Test
    public void formChooserListMakesDirsTest() throws IOException {
        testDirectories();
    }

}
