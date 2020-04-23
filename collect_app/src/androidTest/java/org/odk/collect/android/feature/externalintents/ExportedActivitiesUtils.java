package org.odk.collect.android.feature.externalintents;

import junit.framework.Assert;

import org.odk.collect.android.storage.StoragePathProvider;

import java.io.File;

import timber.log.Timber;

class ExportedActivitiesUtils {

    private ExportedActivitiesUtils() {

    }

    static void clearDirectories() {
        for (String dirName : new StoragePathProvider().getOdkDirPaths()) {
            File dir = new File(dirName);
            if (dir.exists()) {
                if (dir.delete()) {
                    Timber.i("Directory was not deleted");
                }
            }
        }

    }

    static void testDirectories() {
        for (String dirName : new StoragePathProvider().getOdkDirPaths()) {
            File dir = new File(dirName);
            Assert.assertTrue("File " + dirName + "does not exist", dir.exists());
            Assert.assertTrue("File" + dirName + "does not exist", dir.isDirectory());
        }
    }

}
