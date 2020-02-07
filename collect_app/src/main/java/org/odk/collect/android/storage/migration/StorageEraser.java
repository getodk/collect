package org.odk.collect.android.storage.migration;

import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import java.io.File;

class StorageEraser {

    private final StoragePathProvider storagePathProvider;

    StorageEraser(StoragePathProvider storagePathProvider) {
        this.storagePathProvider = storagePathProvider;
    }

    void clearOdkDirOnScopedStorage() {
        deleteDirectory(new File(storagePathProvider.getScopedStorageDirPath(StorageSubdirectory.ODK)));
    }

    void deleteOdkDirFromUnscopedStorage() {
        deleteDirectory(new File(storagePathProvider.getUnscopedStorageDirPath(StorageSubdirectory.ODK)));
    }

    private static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}
