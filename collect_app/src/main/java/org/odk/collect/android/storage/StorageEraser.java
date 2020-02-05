package org.odk.collect.android.storage;

import java.io.File;

class StorageEraser {

    void clearOdkDirOnScopedStorage(StoragePathProvider storagePathProvider) {
        deleteDirectory(new File(storagePathProvider.getScopedStorageDirPath(StorageSubdirectory.ODK)));
    }

    void deleteOdkDirFromUnscopedStorage(StoragePathProvider storagePathProvider) {
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
