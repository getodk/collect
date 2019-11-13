package org.odk.collect.android.utilities;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

public class FileUtilsTest {

    @Test
    public void getAbsoluteFilePathTest() {
        assertNull(FileUtils.getAbsoluteFilePath(null));

        assertEquals("/storage/emulated/0/odk/forms/selectext.xml", FileUtils.getAbsoluteFilePath("/forms/selectext.xml"));
        assertEquals("/storage/emulated/0/odk/forms/selectext.xml", FileUtils.getAbsoluteFilePath("/storage/emulated/0/odk/forms/selectext.xml"));

        assertEquals("/storage/emulated/0/odk/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml", FileUtils.getAbsoluteFilePath("/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml"));
        assertEquals("/storage/emulated/0/odk/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml", FileUtils.getAbsoluteFilePath("/storage/emulated/0/odk/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml"));

        assertEquals("/storage/emulated/0/odk/forms/selectext-media/itemsets.csv", FileUtils.getAbsoluteFilePath("/forms/selectext-media/itemsets.csv"));
        assertEquals("/storage/emulated/0/odk/forms/selectext-media/itemsets.csv", FileUtils.getAbsoluteFilePath("/storage/emulated/0/odk/forms/selectext-media/itemsets.csv"));
    }

    @Test
    public void getRelativeFilePathTest() {
        assertNull(FileUtils.getRelativeFilePath(null));

        assertEquals("/forms/selectext.xml", FileUtils.getRelativeFilePath("/storage/emulated/0/odk/forms/selectext.xml"));
        assertEquals("/forms/selectext.xml", FileUtils.getRelativeFilePath("/forms/selectext.xml"));

        assertEquals("/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml", FileUtils.getRelativeFilePath("/storage/emulated/0/odk/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml"));
        assertEquals("/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml", FileUtils.getRelativeFilePath("/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml"));

        assertEquals("/forms/selectext-media/itemsets.csv", FileUtils.getRelativeFilePath("/storage/emulated/0/odk/forms/selectext-media/itemsets.csv"));
        assertEquals("/forms/selectext-media/itemsets.csv", FileUtils.getRelativeFilePath("/forms/selectext-media/itemsets.csv"));
    }
}
