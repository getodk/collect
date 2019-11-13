package org.odk.collect.android.utilities;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class DatabaseUtilsTest {

    @Test
    public void getAbsoluteFilePathTest() {
        assertEquals("", DatabaseUtils.getAbsoluteFilePath(null));

        assertEquals("/storage/emulated/0/odk/forms/selectext.xml", DatabaseUtils.getAbsoluteFilePath("/forms/selectext.xml"));
        assertEquals("/storage/emulated/0/odk/forms/selectext.xml", DatabaseUtils.getAbsoluteFilePath("/storage/emulated/0/odk/forms/selectext.xml"));

        assertEquals("/storage/emulated/0/odk/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml", DatabaseUtils.getAbsoluteFilePath("/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml"));
        assertEquals("/storage/emulated/0/odk/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml", DatabaseUtils.getAbsoluteFilePath("/storage/emulated/0/odk/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml"));

        assertEquals("/storage/emulated/0/odk/forms/selectext-media/itemsets.csv", DatabaseUtils.getAbsoluteFilePath("/forms/selectext-media/itemsets.csv"));
        assertEquals("/storage/emulated/0/odk/forms/selectext-media/itemsets.csv", DatabaseUtils.getAbsoluteFilePath("/storage/emulated/0/odk/forms/selectext-media/itemsets.csv"));
    }

    @Test
    public void getRelativeFilePathTest() {
        assertEquals("", DatabaseUtils.getRelativeFilePath(null));

        assertEquals("/forms/selectext.xml", DatabaseUtils.getRelativeFilePath("/storage/emulated/0/odk/forms/selectext.xml"));
        assertEquals("/forms/selectext.xml", DatabaseUtils.getRelativeFilePath("/forms/selectext.xml"));

        assertEquals("/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml", DatabaseUtils.getRelativeFilePath("/storage/emulated/0/odk/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml"));
        assertEquals("/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml", DatabaseUtils.getRelativeFilePath("/instances/All widgets_2019-11-13_14-00-22/All widgets_2019-11-13_14-00-22.xml"));

        assertEquals("/forms/selectext-media/itemsets.csv", DatabaseUtils.getRelativeFilePath("/storage/emulated/0/odk/forms/selectext-media/itemsets.csv"));
        assertEquals("/forms/selectext-media/itemsets.csv", DatabaseUtils.getRelativeFilePath("/forms/selectext-media/itemsets.csv"));
    }
}
