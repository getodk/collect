package org.odk.collect.android.externalintents;

import junit.framework.Assert;

import org.odk.collect.android.storage.StorageManager;

import java.io.File;

import timber.log.Timber;

import static org.odk.collect.android.storage.StorageManager.CACHE_PATH;
import static org.odk.collect.android.storage.StorageManager.FORMS_PATH;
import static org.odk.collect.android.storage.StorageManager.INSTANCES_PATH;
import static org.odk.collect.android.storage.StorageManager.METADATA_PATH;
import static org.odk.collect.android.storage.StorageManager.ODK_ROOT;
import static org.odk.collect.android.storage.StorageManager.OFFLINE_LAYERS;

class ExportedActivitiesUtils {

    private static final String[] DIRS = {
            StorageManager.getOdkRoot(),
            StorageManager.getFormsPath(),
            StorageManager.getInstancesPath(),
            StorageManager.getCachePath(),
            StorageManager.getMetadataPath(), OFFLINE_LAYERS
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
