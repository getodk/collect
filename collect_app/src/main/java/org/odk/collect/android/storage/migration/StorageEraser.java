package org.odk.collect.android.storage.migration;

import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import java.io.File;

public class StorageEraser {

    private final StoragePathProvider storagePathProvider;

    public StorageEraser(StoragePathProvider storagePathProvider) {
        this.storagePathProvider = storagePathProvider;
    }

    void clearCache() {
        deleteDirectory(new File(storagePathProvider.getDirPath(StorageSubdirectory.CACHE)));
    }

    void removeItemsetsDb() {
        removeFile(storagePathProvider.getDirPath(StorageSubdirectory.CACHE) + File.separator + ItemsetDbAdapter.DATABASE_NAME);
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

    void removeFile(String filePath) {
        new File(filePath).delete();
    }
}
