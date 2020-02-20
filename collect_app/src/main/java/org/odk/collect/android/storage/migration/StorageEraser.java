package org.odk.collect.android.storage.migration;

import org.odk.collect.android.storage.StoragePathProvider;

import java.io.File;

public class StorageEraser {

    private final StoragePathProvider storagePathProvider;

    public StorageEraser(StoragePathProvider storagePathProvider) {
        this.storagePathProvider = storagePathProvider;
    }

    void clearOdkDirOnScopedStorage() {
        deleteDirectory(new File(storagePathProvider.getScopedStorageRootDirPath()));
    }

    void deleteOdkDirFromUnscopedStorage() {
        deleteDirectory(new File(storagePathProvider.getUnscopedStorageRootDirPath()));
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
