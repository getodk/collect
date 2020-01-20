package org.odk.collect.android.externalintents;

import junit.framework.Assert;

import org.odk.collect.android.storage.StorageManager;

import java.io.File;

import timber.log.Timber;

class ExportedActivitiesUtils {

    private static final String[] DIRS = {
            StorageManager.getOdkRoot(),
            StorageManager.getFormsPath(),
            StorageManager.getInstancesPath(),
            StorageManager.getCachePath(),
            StorageManager.getMetadataPath(),
            StorageManager.getOfflineLayers()
    };

    private ExportedActivitiesUtils() {

    }

    static void clearDirectories() {
        for (String dirName : DIRS) {
            File dir = new File(dirName);
            if (dir.exists()) {
                if (dir.delete()) {
                    Timber.i("Directory was not deleted");
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
