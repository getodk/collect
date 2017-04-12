package org.odk.collect.android.externalintents;

import junit.framework.Assert;

import java.io.File;

import static org.odk.collect.android.application.Collect.CACHE_PATH;
import static org.odk.collect.android.application.Collect.FORMS_PATH;
import static org.odk.collect.android.application.Collect.INSTANCES_PATH;
import static org.odk.collect.android.application.Collect.METADATA_PATH;
import static org.odk.collect.android.application.Collect.ODK_ROOT;
import static org.odk.collect.android.application.Collect.OFFLINE_LAYERS;

class ExportedActivitiesUtils {

    private static String[] DIRS = new String[]{
            ODK_ROOT, FORMS_PATH, INSTANCES_PATH, CACHE_PATH, METADATA_PATH, OFFLINE_LAYERS
    };

    static void clearDirectories() {
        for (String dirName : DIRS) {
            File dir = new File(dirName);
            if (dir.exists()) {
                if (dir.delete()) {
                    System.out.println("Directory was not deleted");
                }
            }
        }

    }

    static void testDirectories() {
        for (String dirName : DIRS) {
            File dir = new File(dirName);
            Assert.assertTrue("File " + dirName + "does not exist", dir.exists());
            Assert.assertTrue("File" + dirName + "does not exist", dir.isDirectory());
        }
    }

}
